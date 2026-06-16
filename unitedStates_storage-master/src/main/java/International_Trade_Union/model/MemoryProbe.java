package International_Trade_Union.model;

import java.util.Map;

public final class MemoryProbe {
    private MemoryProbe() {
    }

    public static boolean isEnabled() {
        return NodeMemoryProfiler.isEnabled();
    }

    public static String beginTrace(String stageName, Long blockIndex) {
        return NodeMemoryProfiler.beginTrace(stageName, blockIndex);
    }

    public static void recordStage(String stageName) {
        NodeMemoryProfiler.recordStage(stageName);
    }

    public static void recordStage(String stageName, Map<String, ?> metrics) {
        NodeMemoryProfiler.recordStage(stageName, metrics);
    }

    public static void endTrace(String stageName) {
        NodeMemoryProfiler.endTrace(stageName);
    }

    public static Map<String, Object> snapshot() {
        return NodeMemoryProfiler.snapshot();
    }

    public static String lastStageName() {
        return NodeMemoryProfiler.lastStageName();
    }
}
