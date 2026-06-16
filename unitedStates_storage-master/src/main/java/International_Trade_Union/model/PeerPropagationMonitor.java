package International_Trade_Union.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PeerPropagationMonitor {
    private static volatile long lastPeerPropagationStartedAt = -1;
    private static volatile long lastPeerPropagationFinishedAt = -1;
    private static volatile String lastPeerPropagationSkippedReason = "none";
    private static volatile int lastPeerPropagationPeerCount = 0;
    private static volatile int lastPeerPropagationSuccessCount = 0;
    private static volatile int lastPeerPropagationFailureCount = 0;
    private static volatile int lastPeerPropagationSelfSkippedCount = 0;
    private static volatile String lastMemoryGuardDecision = "not_checked";
    private static volatile long lastMemoryGuardFreeMemory = -1;
    private static volatile long lastMemoryGuardMaxMemory = -1;
    private static volatile long lastTournamentWinnerIndex = -1;
    private static volatile long lastPersistedBlockIndex = -1;
    private static volatile String lastPersistedBlockHash = "none";
    private static volatile String lastPostPersistAction = "none";
    private static volatile String lastPostPersistError = "none";

    private PeerPropagationMonitor() {
    }

    public static void recordPersistedBlock(long index, String hash) {
        lastPersistedBlockIndex = index;
        lastPersistedBlockHash = hash == null || hash.isBlank() ? "none" : hash;
        lastTournamentWinnerIndex = index;
        lastPostPersistAction = "persisted";
        lastPostPersistError = "none";
    }

    public static void recordMemoryGuard(String decision, long freeMemory, long maxMemory) {
        lastMemoryGuardDecision = decision == null || decision.isBlank() ? "unknown" : decision;
        lastMemoryGuardFreeMemory = freeMemory;
        lastMemoryGuardMaxMemory = maxMemory;
    }

    public static void recordSkipped(String reason, int peerCount, int selfSkippedCount) {
        lastPeerPropagationStartedAt = -1;
        lastPeerPropagationFinishedAt = System.currentTimeMillis();
        lastPeerPropagationSkippedReason = reason == null || reason.isBlank() ? "unknown" : reason;
        lastPeerPropagationPeerCount = Math.max(0, peerCount);
        lastPeerPropagationSuccessCount = 0;
        lastPeerPropagationFailureCount = 0;
        lastPeerPropagationSelfSkippedCount = Math.max(0, selfSkippedCount);
        lastPostPersistAction = "peer_propagation_skipped";
        lastPostPersistError = "none";
    }

    public static void recordStarted(int peerCount, int selfSkippedCount) {
        lastPeerPropagationStartedAt = System.currentTimeMillis();
        lastPeerPropagationFinishedAt = -1;
        lastPeerPropagationSkippedReason = "none";
        lastPeerPropagationPeerCount = Math.max(0, peerCount);
        lastPeerPropagationSuccessCount = 0;
        lastPeerPropagationFailureCount = 0;
        lastPeerPropagationSelfSkippedCount = Math.max(0, selfSkippedCount);
        lastPostPersistAction = "peer_propagation_started";
        lastPostPersistError = "none";
    }

    public static void recordFinished(int peerCount, int successCount, int failureCount, int selfSkippedCount) {
        lastPeerPropagationFinishedAt = System.currentTimeMillis();
        lastPeerPropagationPeerCount = Math.max(0, peerCount);
        lastPeerPropagationSuccessCount = Math.max(0, successCount);
        lastPeerPropagationFailureCount = Math.max(0, failureCount);
        lastPeerPropagationSelfSkippedCount = Math.max(0, selfSkippedCount);
        lastPostPersistAction = "peer_propagation_finished";
        lastPostPersistError = failureCount > 0 ? "peer_failures=" + failureCount : "none";
    }

    public static Map<String, Object> snapshot() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("lastPeerPropagationStartedAt", lastPeerPropagationStartedAt);
        status.put("lastPeerPropagationFinishedAt", lastPeerPropagationFinishedAt);
        status.put("lastPeerPropagationSkippedReason", lastPeerPropagationSkippedReason);
        status.put("lastPeerPropagationPeerCount", lastPeerPropagationPeerCount);
        status.put("lastPeerPropagationSuccessCount", lastPeerPropagationSuccessCount);
        status.put("lastPeerPropagationFailureCount", lastPeerPropagationFailureCount);
        status.put("lastPeerPropagationSelfSkippedCount", lastPeerPropagationSelfSkippedCount);
        status.put("lastMemoryGuardDecision", lastMemoryGuardDecision);
        status.put("lastMemoryGuardFreeMemory", lastMemoryGuardFreeMemory);
        status.put("lastMemoryGuardMaxMemory", lastMemoryGuardMaxMemory);
        status.put("lastTournamentWinnerIndex", lastTournamentWinnerIndex);
        status.put("lastPersistedBlockIndex", lastPersistedBlockIndex);
        status.put("lastPersistedBlockHash", lastPersistedBlockHash);
        status.put("lastPostPersistAction", lastPostPersistAction);
        status.put("lastPostPersistError", lastPostPersistError);
        return status;
    }

    public static void resetForTests() {
        lastPeerPropagationStartedAt = -1;
        lastPeerPropagationFinishedAt = -1;
        lastPeerPropagationSkippedReason = "none";
        lastPeerPropagationPeerCount = 0;
        lastPeerPropagationSuccessCount = 0;
        lastPeerPropagationFailureCount = 0;
        lastPeerPropagationSelfSkippedCount = 0;
        lastMemoryGuardDecision = "not_checked";
        lastMemoryGuardFreeMemory = -1;
        lastMemoryGuardMaxMemory = -1;
        lastTournamentWinnerIndex = -1;
        lastPersistedBlockIndex = -1;
        lastPersistedBlockHash = "none";
        lastPostPersistAction = "none";
        lastPostPersistError = "none";
    }
}
