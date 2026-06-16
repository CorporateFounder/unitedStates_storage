package International_Trade_Union.model;

import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.setings.Seting;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class DiagnosticLogRetention {
    private static final String[] PATTERNS = {
            "citu-memory-probe*.jsonl",
            "citu-memory-forensics*.jsonl",
            "gc-citu-node*.log*",
            "heapdump-citu-node*.hprof",
            "hs_err_pid*.log",
            "citu-node-*-diag-*-stdout.log",
            "citu-node-*-diag-*-stderr.log"
    };
    private static final Object LOCK = new Object();

    private static volatile long lastCleanupBlock = Long.MIN_VALUE;
    private static volatile boolean configured;
    private static volatile boolean enabled = true;
    private static volatile long cleanupEveryBlocks = 100;
    private static volatile long keepBlocks = 5000;
    private static volatile long maxFileBytes = 52_428_800L;
    private static volatile int maxFiles = 20;
    private static volatile Path logDir = Paths.get("C:\\server\\logs");

    private DiagnosticLogRetention() {
    }

    public static void configure(boolean enabledValue,
                                 long cleanupEveryBlocksValue,
                                 long keepBlocksValue,
                                 long maxFileBytesValue,
                                 int maxFilesValue,
                                 String logDirValue) {
        enabled = enabledValue;
        cleanupEveryBlocks = Math.max(1, cleanupEveryBlocksValue);
        keepBlocks = Math.max(1, keepBlocksValue);
        maxFileBytes = Math.max(1024L * 1024L, maxFileBytesValue);
        maxFiles = Math.max(1, maxFilesValue);
        if (logDirValue != null && !logDirValue.isBlank()) {
            logDir = Paths.get(logDirValue);
        }
        configured = true;
        publishSystemProperties();
    }

    public static void cleanupIfDue(long latestBlockIndex) {
        try {
            ensureConfiguredFromSystemProperties();
            if (!enabled || latestBlockIndex < 0) {
                return;
            }
            synchronized (LOCK) {
                if (lastCleanupBlock != Long.MIN_VALUE
                        && latestBlockIndex - lastCleanupBlock < cleanupEveryBlocks) {
                    return;
                }
                lastCleanupBlock = latestBlockIndex;
            }
            cleanupNow();
        } catch (Exception e) {
            MyLogger.warnLog("DIAGNOSTIC_LOG_RETENTION_SKIPPED " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static void cleanupNow() {
        try {
            ensureConfiguredFromSystemProperties();
            if (!enabled || !Files.isDirectory(logDir)) {
                return;
            }
            long cutoffMillis = System.currentTimeMillis() - maxAgeMillis();
            for (String pattern : PATTERNS) {
                List<Path> files = whitelistedFiles(pattern);
                deleteOlderThan(files, cutoffMillis);
                enforceMaxFiles(pattern);
                rotateOversizeDiagnosticFiles(pattern);
            }
        } catch (Exception e) {
            MyLogger.warnLog("DIAGNOSTIC_LOG_RETENTION_FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static void rotateOversizeFile(Path file) {
        try {
            ensureConfiguredFromSystemProperties();
            if (file == null || !isUnderLogDir(file) || !isWhitelisted(file) || !isRotatable(file)) {
                return;
            }
            if (!Files.exists(file) || Files.size(file) <= maxFileBytes) {
                return;
            }
            Path rotated = file.resolveSibling(file.getFileName().toString() + ".1");
            if (Files.exists(rotated)) {
                Files.deleteIfExists(rotated);
            }
            Files.move(file, rotated, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            try {
                if (file != null && isUnderLogDir(file) && isWhitelisted(file) && isRotatable(file)) {
                    Files.deleteIfExists(file);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static String settingsSummary() {
        ensureConfiguredFromSystemProperties();
        return "LOG_RETENTION_ENABLED=" + enabled
                + " cleanupEveryBlocks=" + cleanupEveryBlocks
                + " keepBlocks=" + keepBlocks
                + " maxFiles=" + maxFiles
                + " maxFileBytes=" + maxFileBytes
                + " logDir=" + logDir;
    }

    static void resetForTests(Path testLogDir) {
        configured = true;
        enabled = true;
        cleanupEveryBlocks = 100;
        keepBlocks = 5000;
        maxFileBytes = 52_428_800L;
        maxFiles = 20;
        logDir = testLogDir;
        lastCleanupBlock = Long.MIN_VALUE;
        publishSystemProperties();
    }

    private static void deleteOlderThan(List<Path> files, long cutoffMillis) {
        for (Path file : files) {
            try {
                if (Files.getLastModifiedTime(file).toMillis() < cutoffMillis) {
                    Files.deleteIfExists(file);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void enforceMaxFiles(String pattern) {
        List<Path> files = whitelistedFiles(pattern);
        files.sort(Comparator.comparingLong(DiagnosticLogRetention::lastModifiedMillis).reversed());
        for (int i = maxFiles; i < files.size(); i++) {
            try {
                Files.deleteIfExists(files.get(i));
            } catch (Exception ignored) {
            }
        }
    }

    private static void rotateOversizeDiagnosticFiles(String pattern) {
        for (Path file : whitelistedFiles(pattern)) {
            try {
                if (Files.size(file) > maxFileBytes && isRotatable(file)) {
                    rotateOversizeFile(file);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static List<Path> whitelistedFiles(String pattern) {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir, pattern)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path) && isUnderLogDir(path) && isWhitelisted(path)) {
                    files.add(path);
                }
            }
        } catch (IOException ignored) {
        }
        return files;
    }

    private static boolean isWhitelisted(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return (name.startsWith("citu-memory-probe") && name.endsWith(".jsonl"))
                || (name.startsWith("citu-memory-forensics") && name.endsWith(".jsonl"))
                || (name.startsWith("gc-citu-node") && name.contains(".log"))
                || (name.startsWith("heapdump-citu-node") && name.endsWith(".hprof"))
                || (name.startsWith("hs_err_pid") && name.endsWith(".log"))
                || (name.startsWith("citu-node-") && name.contains("-diag-") && name.endsWith("-stdout.log"))
                || (name.startsWith("citu-node-") && name.contains("-diag-") && name.endsWith("-stderr.log"));
    }

    private static boolean isRotatable(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".jsonl") || name.contains(".log");
    }

    private static boolean isUnderLogDir(Path file) {
        try {
            return file.toAbsolutePath().normalize().startsWith(logDir.toAbsolutePath().normalize());
        } catch (Exception e) {
            return false;
        }
    }

    private static long lastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return Instant.EPOCH.toEpochMilli();
        }
    }

    private static long maxAgeMillis() {
        long blockSeconds = 100L;
        try {
            blockSeconds = Math.max(1L, Seting.BLOCK_GENERATION_INTERVAL / 1000L);
        } catch (Exception ignored) {
        }
        return keepBlocks * blockSeconds * 1000L;
    }

    private static void ensureConfiguredFromSystemProperties() {
        if (configured) {
            return;
        }
        enabled = Boolean.parseBoolean(System.getProperty("citu.logs.cleanup.enabled", "true"));
        cleanupEveryBlocks = Math.max(1, Long.getLong("citu.logs.cleanupEveryBlocks", 100L));
        keepBlocks = Math.max(1, Long.getLong("citu.logs.keepBlocks", 5000L));
        maxFileBytes = Math.max(1024L * 1024L, Long.getLong("citu.logs.maxFileBytes", 52_428_800L));
        maxFiles = Math.max(1, Integer.getInteger("citu.logs.maxFiles", 20));
        logDir = Paths.get(System.getProperty("citu.logs.dir", "C:\\server\\logs"));
        configured = true;
        publishSystemProperties();
    }

    private static void publishSystemProperties() {
        System.setProperty("citu.logs.cleanup.enabled", String.valueOf(enabled));
        System.setProperty("citu.logs.cleanupEveryBlocks", String.valueOf(cleanupEveryBlocks));
        System.setProperty("citu.logs.keepBlocks", String.valueOf(keepBlocks));
        System.setProperty("citu.logs.maxFileBytes", String.valueOf(maxFileBytes));
        System.setProperty("citu.logs.maxFiles", String.valueOf(maxFiles));
        System.setProperty("citu.logs.dir", String.valueOf(logDir));
    }
}
