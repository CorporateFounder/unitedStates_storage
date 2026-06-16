package International_Trade_Union.model;

import International_Trade_Union.logger.MyLogger;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

public final class NodeMemoryProfiler {
    private static final int DEFAULT_LAST_TRACES = 3;
    private static final int MAX_SAMPLES_PER_TRACE = 200;
    private static final int MAX_STAGE_SAMPLES_IN_SNAPSHOT = 10;
    private static final int MAX_STRING_VALUE_LENGTH = 160;
    public static final long JSONL_MAX_BYTES = 50L * 1024L * 1024L;
    private static final String DEFAULT_JSONL_PATH = "C:\\server\\logs\\citu-memory-forensics.jsonl";

    private static final Object LOCK = new Object();
    private static final Deque<Trace> LAST_TRACES = new ArrayDeque<>();
    private static final ThreadLocal<Trace> CURRENT_TRACE = new ThreadLocal<>();
    private static volatile Map<String, Object> lastHighWatermark = Collections.emptyMap();

    private NodeMemoryProfiler() {
    }

    public static boolean isEnabled() {
        return Boolean.getBoolean("citu.memoryForensics.enabled");
    }

    public static boolean isFileOutputEnabled() {
        return Boolean.getBoolean("citu.memoryForensics.file.enabled");
    }

    public static boolean isVerbose() {
        return Boolean.getBoolean("citu.memoryForensics.verbose");
    }

    public static int configuredLastTraces() {
        return Math.max(1, Integer.getInteger("citu.memoryForensics.lastTraces", DEFAULT_LAST_TRACES));
    }

    public static String beginTrace(String stageName, Long blockIndex) {
        if (!isEnabled()) {
            return "";
        }
        Trace trace = new Trace(UUID.randomUUID().toString(), blockIndex, Thread.currentThread().getName());
        CURRENT_TRACE.set(trace);
        synchronized (LOCK) {
            LAST_TRACES.addLast(trace);
            trimTraces();
        }
        recordStage(stageName, Collections.emptyMap());
        return trace.traceId;
    }

    public static void endTrace(String stageName) {
        if (!isEnabled()) {
            return;
        }
        recordStage(stageName, Collections.emptyMap());
        CURRENT_TRACE.remove();
    }

    public static void recordStage(String stageName) {
        recordStage(stageName, Collections.emptyMap());
    }

    public static void recordStage(String stageName, Map<String, ?> metrics) {
        if (!isEnabled()) {
            return;
        }
        Trace trace = CURRENT_TRACE.get();
        if (trace == null) {
            beginTrace(stageName + " trace", null);
            trace = CURRENT_TRACE.get();
            if (trace == null) {
                return;
            }
        }

        StageSample sample = createSample(trace, stageName, metrics);
        synchronized (LOCK) {
            if (trace.samples.size() >= MAX_SAMPLES_PER_TRACE) {
                trace.samples.removeFirst();
            }
            trace.samples.addLast(sample);
            trace.lastStage = stageName;
            trace.largestDeltaSample = largerDelta(trace.largestDeltaSample, sample);
        }
        checkHighWatermark(trace, sample);
        MyLogger.saveLog("[MEMORY_PROBE] traceId=" + trace.traceId
                + " stage=" + sample.stageName
                + " blockIndex=" + trace.blockIndex
                + " usedMemory=" + sample.usedMemory
                + " usedDeltaFromPreviousStage=" + sample.usedDeltaFromPreviousStage
                + " durationMs=" + sample.durationMs);
        writeJsonlIfEnabled(trace, sample);
    }

    public static Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", isEnabled());
        result.put("configuredLastTraces", configuredLastTraces());
        synchronized (LOCK) {
            result.put("lastTracesCount", LAST_TRACES.size());
            List<Map<String, Object>> traces = new ArrayList<>();
            StageSample largestAcross = null;
            for (Trace trace : LAST_TRACES) {
                traces.add(trace.toMap());
                largestAcross = largerDelta(largestAcross, trace.largestDeltaSample);
            }
            result.put("lastTraces", traces);
            result.put("largestDeltaStageAcrossLast3", largestAcross == null ? null : largestAcross.toSummaryMap());
            Trace current = CURRENT_TRACE.get();
            result.put("currentTraceSummary", current == null ? null : current.toSummaryMap());
            StageSample largestLastTrace = LAST_TRACES.isEmpty() ? null : LAST_TRACES.peekLast().largestDeltaSample;
            result.put("largestDeltaStageLastTrace", largestLastTrace == null
                    ? null
                    : largestLastTrace.toSummaryMap());
            result.put("largestDeltaBytesLastTrace", largestLastTrace == null ? 0 : largestLastTrace.usedDeltaFromPreviousStage);
            result.put("largestDeltaBytesAcrossLast3", largestAcross == null ? 0 : largestAcross.usedDeltaFromPreviousStage);
        }
        result.put("lastHighWatermark", lastHighWatermark);
        Object suspectedStage = null;
        Object suspectedReason = null;
        Map<String, Object> largest = castMap(result.get("largestDeltaStageAcrossLast3"));
        if (largest != null) {
            suspectedStage = largest.get("stageName");
            suspectedReason = "largest positive used-memory delta across retained traces";
        }
        result.put("suspectedStage", suspectedStage);
        result.put("suspectedReason", suspectedReason);
        return result;
    }

    public static Map<String, Object> memoryStatusFields() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("memoryForensicsEnabled", isEnabled());
        synchronized (LOCK) {
            Trace last = LAST_TRACES.peekLast();
            status.put("lastForensicsTraceId", last == null ? null : last.traceId);
            status.put("lastForensicsStage", last == null ? null : last.lastStage);
            StageSample largest = last == null ? null : last.largestDeltaSample;
            status.put("lastForensicsLargestDeltaStage", largest == null ? null : largest.stageName);
            status.put("lastForensicsLargestDeltaBytes", largest == null ? 0 : largest.usedDeltaFromPreviousStage);
        }
        status.put("lastForensicsHighWatermark", lastHighWatermark);
        return status;
    }

    public static String lastStageName() {
        synchronized (LOCK) {
            Trace last = LAST_TRACES.peekLast();
            return last == null ? "none" : String.valueOf(last.lastStage);
        }
    }

    public static void clearForTests() {
        synchronized (LOCK) {
            LAST_TRACES.clear();
        }
        CURRENT_TRACE.remove();
        lastHighWatermark = Collections.emptyMap();
    }

    public static Map<String, Object> metrics(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (keyValues == null) {
            return map;
        }
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), sanitizeValue(keyValues[i + 1]));
        }
        return map;
    }

    private static StageSample createSample(Trace trace, String stageName, Map<String, ?> metrics) {
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long now = System.currentTimeMillis();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        long max = runtime.maxMemory();
        StageSample previous = trace.samples.peekLast();
        long previousUsed = previous == null ? trace.startUsedMemory : previous.usedMemory;
        long previousTime = previous == null ? trace.startMillis : previous.timestampMillis;
        Map<String, Object> sanitizedMetrics = new LinkedHashMap<>();
        if (metrics != null) {
            metrics.forEach((key, value) -> sanitizedMetrics.put(String.valueOf(key), sanitizeValue(value)));
        }
        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        return new StageSample(
                Instant.ofEpochMilli(now).toString(),
                now,
                stageName,
                used,
                free,
                total,
                max,
                used - previousUsed,
                used - trace.startUsedMemory,
                threadMXBean.getThreadCount(),
                commonPool.getParallelism(),
                commonPool.getPoolSize(),
                commonPool.getActiveThreadCount(),
                now - previousTime,
                sanitizedMetrics
        );
    }

    private static Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        String text = String.valueOf(value);
        if (text.length() <= MAX_STRING_VALUE_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_STRING_VALUE_LENGTH) + "...[truncated]";
    }

    private static void checkHighWatermark(Trace trace, StageSample sample) {
        if (sample.maxMemory <= 0) {
            return;
        }
        int percent = (int) ((sample.usedMemory * 100L) / sample.maxMemory);
        int threshold = percent >= 95 ? 95 : percent >= 90 ? 90 : percent >= 80 ? 80 : percent >= 70 ? 70 : 0;
        if (threshold == 0 || threshold <= trace.highestWatermarkPercent) {
            return;
        }
        trace.highestWatermarkPercent = threshold;
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("thresholdPercent", threshold);
        snapshot.put("traceId", trace.traceId);
        snapshot.put("blockIndex", trace.blockIndex);
        snapshot.put("stageName", sample.stageName);
        snapshot.put("usedMemory", sample.usedMemory);
        snapshot.put("maxMemory", sample.maxMemory);
        snapshot.put("usedDeltaFromPreviousStage", sample.usedDeltaFromPreviousStage);
        snapshot.put("largestDeltaStage", trace.largestDeltaSample == null ? null : trace.largestDeltaSample.toSummaryMap());
        snapshot.put("recentStageSamples", trace.lastSamples(MAX_STAGE_SAMPLES_IN_SNAPSHOT));
        lastHighWatermark = snapshot;
        MyLogger.warnLog("MEMORY_FORENSICS_HIGH_WATERMARK threshold=" + threshold
                + " traceId=" + trace.traceId
                + " stage=" + sample.stageName
                + " usedMemory=" + sample.usedMemory
                + " maxMemory=" + sample.maxMemory);
    }

    private static void writeJsonlIfEnabled(Trace trace, StageSample sample) {
        if (!isFileOutputEnabled()) {
            return;
        }
        Path path = Paths.get(System.getProperty("citu.memoryForensics.file.path", DEFAULT_JSONL_PATH));
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.exists(path) && Files.size(path) > maxJsonlBytes()) {
                DiagnosticLogRetention.rotateOversizeFile(path);
            }
            String line = compactJson(trace, sample) + System.lineSeparator();
            Files.write(path, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            if (isVerbose()) {
                MyLogger.warnLog("memory forensics file output skipped: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private static long maxJsonlBytes() {
        return Math.max(1024L * 1024L, Long.getLong("citu.logs.maxFileBytes", JSONL_MAX_BYTES));
    }

    private static String compactJson(Trace trace, StageSample sample) {
        return "{"
                + "\"traceId\":\"" + escape(trace.traceId) + "\","
                + "\"blockIndex\":" + (trace.blockIndex == null ? "null" : trace.blockIndex) + ","
                + "\"stageName\":\"" + escape(sample.stageName) + "\","
                + "\"usedMemory\":" + sample.usedMemory + ","
                + "\"usedDeltaFromPreviousStage\":" + sample.usedDeltaFromPreviousStage + ","
                + "\"durationMs\":" + sample.durationMs + ","
                + "\"metrics\":\"" + escape(String.valueOf(sample.metrics)) + "\""
                + "}";
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void trimTraces() {
        int max = configuredLastTraces();
        while (LAST_TRACES.size() > max) {
            LAST_TRACES.removeFirst();
        }
    }

    private static StageSample largerDelta(StageSample left, StageSample right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return right.usedDeltaFromPreviousStage > left.usedDeltaFromPreviousStage ? right : left;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private static final class Trace {
        private final String traceId;
        private final Long blockIndex;
        private final String threadName;
        private final long startMillis;
        private final long startUsedMemory;
        private final Deque<StageSample> samples = new ArrayDeque<>();
        private String lastStage;
        private StageSample largestDeltaSample;
        private int highestWatermarkPercent;

        private Trace(String traceId, Long blockIndex, String threadName) {
            this.traceId = traceId;
            this.blockIndex = blockIndex;
            this.threadName = threadName;
            this.startMillis = System.currentTimeMillis();
            Runtime runtime = Runtime.getRuntime();
            this.startUsedMemory = runtime.totalMemory() - runtime.freeMemory();
        }

        private Map<String, Object> toMap() {
            Map<String, Object> map = toSummaryMap();
            map.put("largestDeltaStageLastTrace", largestDeltaSample == null ? null : largestDeltaSample.toSummaryMap());
            map.put("stageSamples", lastSamples(MAX_STAGE_SAMPLES_IN_SNAPSHOT));
            return map;
        }

        private Map<String, Object> toSummaryMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("traceId", traceId);
            map.put("blockIndex", blockIndex);
            map.put("threadName", threadName);
            map.put("startedAt", Instant.ofEpochMilli(startMillis).toString());
            map.put("lastStage", lastStage);
            map.put("sampleCount", samples.size());
            return map;
        }

        private List<Map<String, Object>> lastSamples(int count) {
            List<StageSample> copy = new ArrayList<>(samples);
            int from = Math.max(0, copy.size() - count);
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = from; i < copy.size(); i++) {
                result.add(copy.get(i).toMap());
            }
            return result;
        }
    }

    private static final class StageSample {
        private final String timestamp;
        private final long timestampMillis;
        private final String stageName;
        private final long usedMemory;
        private final long freeMemory;
        private final long totalMemory;
        private final long maxMemory;
        private final long usedDeltaFromPreviousStage;
        private final long usedDeltaFromTraceStart;
        private final int threadCount;
        private final int commonPoolParallelism;
        private final int commonPoolSize;
        private final int commonPoolActiveThreadCount;
        private final long durationMs;
        private final Map<String, Object> metrics;

        private StageSample(String timestamp,
                            long timestampMillis,
                            String stageName,
                            long usedMemory,
                            long freeMemory,
                            long totalMemory,
                            long maxMemory,
                            long usedDeltaFromPreviousStage,
                            long usedDeltaFromTraceStart,
                            int threadCount,
                            int commonPoolParallelism,
                            int commonPoolSize,
                            int commonPoolActiveThreadCount,
                            long durationMs,
                            Map<String, Object> metrics) {
            this.timestamp = timestamp;
            this.timestampMillis = timestampMillis;
            this.stageName = stageName;
            this.usedMemory = usedMemory;
            this.freeMemory = freeMemory;
            this.totalMemory = totalMemory;
            this.maxMemory = maxMemory;
            this.usedDeltaFromPreviousStage = usedDeltaFromPreviousStage;
            this.usedDeltaFromTraceStart = usedDeltaFromTraceStart;
            this.threadCount = threadCount;
            this.commonPoolParallelism = commonPoolParallelism;
            this.commonPoolSize = commonPoolSize;
            this.commonPoolActiveThreadCount = commonPoolActiveThreadCount;
            this.durationMs = durationMs;
            this.metrics = metrics;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> map = toSummaryMap();
            map.put("freeMemory", freeMemory);
            map.put("totalMemory", totalMemory);
            map.put("maxMemory", maxMemory);
            map.put("usedDeltaFromTraceStart", usedDeltaFromTraceStart);
            map.put("threadCount", threadCount);
            map.put("commonPoolParallelism", commonPoolParallelism);
            map.put("commonPoolSize", commonPoolSize);
            map.put("commonPoolActiveThreadCount", commonPoolActiveThreadCount);
            map.put("metrics", metrics);
            return map;
        }

        private Map<String, Object> toSummaryMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("timestamp", timestamp);
            map.put("stageName", stageName);
            map.put("usedMemory", usedMemory);
            map.put("usedDeltaFromPreviousStage", usedDeltaFromPreviousStage);
            map.put("durationMs", durationMs);
            return map;
        }
    }
}
