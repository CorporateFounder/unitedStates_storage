package International_Trade_Union.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticWatcherScriptTest {
    @Test
    void watcherWritesHeartbeatCapturesRequiredEndpointsAndPrunesFiles() throws IOException {
        String script = Files.readString(Path.of("scripts", "v35-diagnostic-watch-node.ps1"));

        assertTrue(script.contains("WATCHER_HEARTBEAT.txt"));
        assertTrue(script.contains("/debug/node/runtime"));
        assertTrue(script.contains("/debug/memory/forensics"));
        assertTrue(script.contains("/debug/block-submit/status"));
        assertTrue(script.contains("/debug/block-submit/traces"));
        assertTrue(script.contains("/debug/tournament/traces"));
        assertTrue(script.contains("/debug/add-block/traces"));
        assertTrue(script.contains("/v35/protocol"));
        assertTrue(script.contains("Select-Object -Skip $MaxFiles"));
        assertTrue(script.contains(".ERROR.txt"));
    }

    @Test
    void diagnosticStartScriptDoesNotDeleteResourcesOrEnableHeapDump() throws IOException {
        String script = Files.readString(Path.of("scripts", "start-node-1.0.9-diagnostic.ps1"));

        assertTrue(script.contains("citu.memoryForensics.enabled=true"));
        assertTrue(script.contains("v35-diagnostic-watch-node.ps1"));
        assertTrue(script.contains("citu-node-v35-activation-514291"));
        assertFalse(script.contains("HeapDumpOnOutOfMemoryError"));
        assertFalse(script.contains("HeapDumpPath"));
        assertFalse(script.contains("Remove-Item -Recurse C:\\resources"));
        assertFalse(script.contains("Remove-Item -Recurse C:\\server\\resources"));
    }

    @Test
    void diagnosticOneTenStartScriptDownloadsVerifiesAndStartsWatcherWithoutHeapDump() throws IOException {
        String script = Files.readString(Path.of("scripts", "start-node-1.0.10-diagnostic.ps1"));

        assertTrue(script.contains("Invoke-WebRequest"));
        assertTrue(script.contains("Get-FileHash -Algorithm SHA256"));
        assertTrue(script.contains("citu.memoryForensics.enabled=true"));
        assertTrue(script.contains("v35-diagnostic-watch-node.ps1"));
        assertTrue(script.contains("MaxFiles"));
        assertTrue(script.contains("/size"));
        assertTrue(script.contains("citu-node-v35-activation-514291"));
        assertFalse(script.contains("HeapDumpOnOutOfMemoryError"));
        assertFalse(script.contains("HeapDumpPath"));
        assertFalse(script.contains("Remove-Item -Recurse C:\\resources"));
        assertFalse(script.contains("Remove-Item -Recurse C:\\server\\resources"));
    }
}
