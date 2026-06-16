package International_Trade_Union.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticLogRetentionTest {
    @TempDir
    Path tempDir;

    @Test
    void deletesOnlyWhitelistedOldDiagnosticFiles() throws Exception {
        DiagnosticLogRetention.resetForTests(tempDir);
        DiagnosticLogRetention.configure(true, 1, 1, 1024 * 1024, 20, tempDir.toString());
        Path oldDiagnostic = tempDir.resolve("citu-memory-probe.jsonl");
        Path businessLog = tempDir.resolve("business.log");
        Files.writeString(oldDiagnostic, "{}", StandardCharsets.UTF_8);
        Files.writeString(businessLog, "keep", StandardCharsets.UTF_8);
        FileTime old = FileTime.fromMillis(System.currentTimeMillis() - 500_000_000L);
        Files.setLastModifiedTime(oldDiagnostic, old);
        Files.setLastModifiedTime(businessLog, old);

        DiagnosticLogRetention.cleanupIfDue(100);

        assertFalse(Files.exists(oldDiagnostic));
        assertTrue(Files.exists(businessLog));
    }

    @Test
    void rotatesOversizeJsonlWithoutTouchingNonWhitelistedFiles() throws Exception {
        DiagnosticLogRetention.resetForTests(tempDir);
        DiagnosticLogRetention.configure(true, 1, 5000, 1024 * 1024, 20, tempDir.toString());
        Path diagnostic = tempDir.resolve("citu-memory-forensics.jsonl");
        Path other = tempDir.resolve("application.log");
        Files.write(diagnostic, new byte[2 * 1024 * 1024]);
        Files.write(other, new byte[2 * 1024 * 1024]);

        DiagnosticLogRetention.cleanupIfDue(100);

        assertFalse(Files.exists(diagnostic));
        assertTrue(Files.exists(tempDir.resolve("citu-memory-forensics.jsonl.1")));
        assertTrue(Files.exists(other));
    }
}
