package International_Trade_Union.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryProbeTest {
    @AfterEach
    void tearDown() {
        System.clearProperty("citu.memoryForensics.enabled");
        NodeMemoryProfiler.clearForTests();
    }

    @Test
    void delegatesToBoundedNodeMemoryProfilerWithoutKeepingPayloads() {
        System.setProperty("citu.memoryForensics.enabled", "true");

        MemoryProbe.beginTrace("resolve_conflict request received", 514291L);
        MemoryProbe.recordStage("resolve_conflict after request body/block parsed",
                NodeMemoryProfiler.metrics("payloadPreview", "x".repeat(1000), "blockCount", 1));
        MemoryProbe.endTrace("resolve_conflict finish");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> traces = (List<Map<String, Object>>) MemoryProbe.snapshot().get("lastTraces");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> samples = (List<Map<String, Object>>) traces.get(0).get("stageSamples");
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) samples.get(1).get("metrics");

        assertEquals(1, traces.size());
        assertTrue(String.valueOf(metrics.get("payloadPreview")).endsWith("...[truncated]"));
        assertEquals("resolve_conflict finish", MemoryProbe.lastStageName());
    }
}
