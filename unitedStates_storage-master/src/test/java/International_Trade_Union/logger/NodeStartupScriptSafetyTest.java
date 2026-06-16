package International_Trade_Union.logger;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeStartupScriptSafetyTest {

    private static final Path RELEASE_DIR = Path.of("..", "release-dryrun", "v35-514291");

    @Test
    void defaultWindowsStartScriptDoesNotEnableHeapDump() throws IOException {
        String script = Files.readString(Path.of("..", "Cstart_CITU_NODE_1_0_13_STABLE.bat"), StandardCharsets.UTF_8);

        assertFalse(script.contains("HeapDumpOnOutOfMemoryError"));
        assertFalse(script.contains("HeapDumpPath"));
        assertTrue(script.contains("citu-node-v35-activation-514291-1.0.13.jar"));
        assertTrue(script.contains("-Dcitu.memoryForensics.enabled=false"));
        assertTrue(script.contains("-Dcitu.memoryForensics.file.enabled=false"));
        assertTrue(script.contains("-Dcitu.diagnostic.traces.enabled=false"));
        assertTrue(script.contains("-Dcitu.prePersistPeerPropagation.enabled=false"));
        assertTrue(script.contains("-Dcitu.peer.maxPeersPerCycle=3"));
        assertTrue(script.contains("-Dcitu.peer.maxRemoteCallsPerMinute=60"));
        assertTrue(script.contains("-Dcitu.peer.maxHealthEntries=32"));
        assertTrue(script.contains("-Dcitu.peer.maxErrorEntries=32"));
        assertTrue(script.contains("-Dcitu.peer.maxBlockedHostEntries=32"));
        assertTrue(script.contains("-Dcitu.memoryGuard.skipPeerWorkPercent=70"));
        assertTrue(script.contains("-Dcitu.memoryGuard.emergencyPeerWorkPercent=85"));
    }

    @Test
    void defaultLinuxStartScriptDoesNotEnableHeapDump() throws IOException {
        String script = Files.readString(RELEASE_DIR.resolve("start-node-linux.sh"), StandardCharsets.UTF_8);

        assertFalse(script.contains("HeapDumpOnOutOfMemoryError"));
        assertFalse(script.contains("HeapDumpPath"));
        assertTrue(script.contains("citu-node-v35-activation-514291-1.0.13.jar"));
        assertTrue(script.contains("-Dcitu.memoryForensics.enabled=false"));
        assertTrue(script.contains("-Dcitu.memoryForensics.file.enabled=false"));
        assertTrue(script.contains("-Dcitu.diagnostic.traces.enabled=false"));
        assertTrue(script.contains("-Dcitu.prePersistPeerPropagation.enabled=false"));
    }

    @Test
    void diagnosticHeapDumpScriptIsExplicitGuardedAndUnique() throws IOException {
        String script = Files.readString(RELEASE_DIR.resolve("start-node-diagnostic-heapdump-windows.bat"), StandardCharsets.UTF_8);

        assertTrue(script.contains("HeapDumpOnOutOfMemoryError"));
        assertTrue(script.contains("HeapDumpPath=C:\\server\\logs\\citu-node-v35-514291-%TS%.hprof")
                || script.contains("HeapDumpPath=%LOG_DIR%\\citu-node-v35-514291-%TS%.hprof"));
        assertTrue(script.contains("Get-Date -Format yyyyMMdd_HHmmss"));
        assertTrue(script.contains("Get-PSDrive -Name C"));
        assertTrue(script.contains("5GB"));
        assertTrue(script.contains("AddDays(-1)") || script.contains("/d -1"));
        assertTrue(script.contains("*.hprof"));
    }

    @Test
    void constantsRemainActivationPreparedAndBurnPlaceholder() {
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    @Test
    void stableApplicationPropertiesKeepDiagnosticsOffByDefault() throws IOException {
        String properties = Files.readString(Path.of("src", "main", "resources", "application.properties"), StandardCharsets.UTF_8);

        assertTrue(properties.contains("citu.memoryForensics.enabled=true"));
        assertTrue(properties.contains("citu.memoryForensics.file.enabled=true"));
        assertTrue(properties.contains("citu.memoryForensics.verbose=false"));
        assertTrue(properties.contains("citu.diagnostic.traces.enabled=true"));
        assertTrue(properties.contains("citu.diagnostic.file.enabled=false"));
        assertTrue(properties.contains("citu.logs.cleanup.enabled=true"));
        assertTrue(properties.contains("citu.logs.cleanupEveryBlocks=100"));
        assertTrue(properties.contains("citu.logs.keepBlocks=5000"));
        assertTrue(properties.contains("citu.logs.maxFileBytes=52428800"));
        assertTrue(properties.contains("citu.logs.maxFiles=20"));
        assertTrue(properties.contains("citu.peer.maxPeersPerCycle=3"));
        assertTrue(properties.contains("citu.peer.maxRemoteCallsPerCycle=6"));
        assertTrue(properties.contains("citu.peer.maxRemoteCallsPerMinute=60"));
        assertTrue(properties.contains("citu.peer.maxHealthEntries=32"));
        assertTrue(properties.contains("citu.peer.maxErrorEntries=32"));
        assertTrue(properties.contains("citu.peer.maxBlockedHostEntries=32"));
        assertTrue(properties.contains("citu.memoryGuard.skipPeerWorkPercent=70"));
        assertTrue(properties.contains("citu.memoryGuard.emergencyPeerWorkPercent=85"));
    }
}
