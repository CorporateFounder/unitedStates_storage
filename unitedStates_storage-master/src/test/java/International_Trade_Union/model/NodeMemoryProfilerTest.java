package International_Trade_Union.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeMemoryProfilerTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("citu.memoryForensics.enabled");
        System.clearProperty("citu.memoryForensics.lastTraces");
        System.clearProperty("citu.memoryForensics.file.enabled");
        NodeMemoryProfiler.clearForTests();
    }

    @Test
    void disabledByDefaultDoesNotStoreTraces() {
        NodeMemoryProfiler.beginTrace("resolve_conflict start", 514290L);
        NodeMemoryProfiler.recordStage("after payload", NodeMemoryProfiler.metrics("responseBytes", 128));
        NodeMemoryProfiler.endTrace("finish");

        Map<String, Object> snapshot = NodeMemoryProfiler.snapshot();

        assertFalse((Boolean) snapshot.get("enabled"));
        assertEquals(0, snapshot.get("lastTracesCount"));
        assertNull(snapshot.get("suspectedStage"));
    }

    @Test
    void keepsOnlyConfiguredLastThreeTraces() {
        System.setProperty("citu.memoryForensics.enabled", "true");
        System.setProperty("citu.memoryForensics.lastTraces", "3");

        for (long index = 1; index <= 5; index++) {
            NodeMemoryProfiler.beginTrace("trace " + index, index);
            NodeMemoryProfiler.recordStage("stage " + index, NodeMemoryProfiler.metrics("winnerListSize", index));
            NodeMemoryProfiler.endTrace("finish " + index);
        }

        Map<String, Object> snapshot = NodeMemoryProfiler.snapshot();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> traces = (List<Map<String, Object>>) snapshot.get("lastTraces");

        assertEquals(3, snapshot.get("lastTracesCount"));
        assertEquals(3L, traces.get(0).get("blockIndex"));
        assertEquals(5L, traces.get(2).get("blockIndex"));
        assertTrue(snapshot.containsKey("largestDeltaStageAcrossLast3"));
        assertTrue(snapshot.containsKey("largestDeltaBytesLastTrace"));
        assertTrue(snapshot.containsKey("largestDeltaBytesAcrossLast3"));
    }

    @Test
    void truncatesLargeMetricTextInsteadOfRetainingPayload() {
        System.setProperty("citu.memoryForensics.enabled", "true");
        String largeJson = "x".repeat(1_000);

        NodeMemoryProfiler.beginTrace("UtilUrl trace", null);
        NodeMemoryProfiler.recordStage("UtilUrl after remote GET", NodeMemoryProfiler.metrics(
                "jsonStringLength", largeJson.length(),
                "payloadPreview", largeJson
        ));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> traces = (List<Map<String, Object>>) NodeMemoryProfiler.snapshot().get("lastTraces");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> samples = (List<Map<String, Object>>) traces.get(0).get("stageSamples");
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) samples.get(samples.size() - 1).get("metrics");

        assertEquals(largeJson.length(), metrics.get("jsonStringLength"));
        assertTrue(String.valueOf(metrics.get("payloadPreview")).endsWith("...[truncated]"));
        assertTrue(String.valueOf(metrics.get("payloadPreview")).length() < largeJson.length());
    }
}
