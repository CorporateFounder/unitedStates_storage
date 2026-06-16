package International_Trade_Union.model;

import International_Trade_Union.logger.MyLogger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class PeerWorkMemoryGuard {
    private static final AtomicLong SKIPPED_COUNT = new AtomicLong();
    private static final AtomicLong EMERGENCY_SKIPPED_COUNT = new AtomicLong();
    private static volatile String lastDecision = "allow";
    private static volatile String lastPhase = "none";
    private static volatile long lastUsedMemory;
    private static volatile long lastMaxMemory;
    private static volatile int lastUsedPercent;
    private static volatile long lastLoggedAt;

    private PeerWorkMemoryGuard() {
    }

    public static boolean shouldSkipNonCriticalPeerWork(String phase) {
        Decision decision = currentDecision(phase);
        if (!decision.skip) {
            lastDecision = "allow";
            lastPhase = safe(phase);
            lastUsedMemory = decision.usedMemory;
            lastMaxMemory = decision.maxMemory;
            lastUsedPercent = decision.usedPercent;
            return false;
        }

        lastDecision = decision.emergency ? "emergency_skip" : "skip";
        lastPhase = safe(phase);
        lastUsedMemory = decision.usedMemory;
        lastMaxMemory = decision.maxMemory;
        lastUsedPercent = decision.usedPercent;
        if (decision.emergency) {
            EMERGENCY_SKIPPED_COUNT.incrementAndGet();
            PeerHealthRegistry.clearTransientPeerBuffers();
            maybeLog("MEMORY_GUARD_EMERGENCY_PEER_WORK_DISABLED", decision);
        } else {
            SKIPPED_COUNT.incrementAndGet();
            maybeLog("MEMORY_GUARD_SKIPPED_NON_CRITICAL_PEER_WORK", decision);
        }
        return true;
    }

    static boolean shouldSkipForMemory(long usedMemory, long maxMemory) {
        return usedPercent(usedMemory, maxMemory) >= PeerNetworkPolicy.memoryGuardSkipPercent();
    }

    static boolean shouldEmergencySkipForMemory(long usedMemory, long maxMemory) {
        return usedPercent(usedMemory, maxMemory) >= PeerNetworkPolicy.memoryGuardEmergencyPercent();
    }

    public static Map<String, Object> snapshot() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("peerWorkMemoryGuardDecision", lastDecision);
        status.put("peerWorkMemoryGuardPhase", lastPhase);
        status.put("peerWorkMemoryGuardUsedMemory", lastUsedMemory);
        status.put("peerWorkMemoryGuardMaxMemory", lastMaxMemory);
        status.put("peerWorkMemoryGuardUsedPercent", lastUsedPercent);
        status.put("peerWorkMemoryGuardSkipPercent", PeerNetworkPolicy.memoryGuardSkipPercent());
        status.put("peerWorkMemoryGuardEmergencyPercent", PeerNetworkPolicy.memoryGuardEmergencyPercent());
        status.put("peerWorkMemoryGuardSkippedCount", SKIPPED_COUNT.get());
        status.put("peerWorkMemoryGuardEmergencySkippedCount", EMERGENCY_SKIPPED_COUNT.get());
        return status;
    }

    public static void resetForTests() {
        SKIPPED_COUNT.set(0);
        EMERGENCY_SKIPPED_COUNT.set(0);
        lastDecision = "allow";
        lastPhase = "none";
        lastUsedMemory = 0;
        lastMaxMemory = 0;
        lastUsedPercent = 0;
        lastLoggedAt = 0;
    }

    private static Decision currentDecision(String phase) {
        Runtime runtime = Runtime.getRuntime();
        long used = Math.max(0, runtime.totalMemory() - runtime.freeMemory());
        long max = runtime.maxMemory();
        int percent = usedPercent(used, max);
        boolean emergency = percent >= PeerNetworkPolicy.memoryGuardEmergencyPercent();
        boolean skip = emergency || percent >= PeerNetworkPolicy.memoryGuardSkipPercent();
        return new Decision(safe(phase), used, max, percent, skip, emergency);
    }

    private static int usedPercent(long usedMemory, long maxMemory) {
        if (maxMemory <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.max(0, (usedMemory * 100L) / maxMemory));
    }

    private static void maybeLog(String event, Decision decision) {
        long now = System.currentTimeMillis();
        if (now - lastLoggedAt < 60_000L) {
            return;
        }
        lastLoggedAt = now;
        MyLogger.warnLog(event
                + " phase=" + decision.phase
                + " usedPercent=" + decision.usedPercent
                + " usedMemory=" + decision.usedMemory
                + " maxMemory=" + decision.maxMemory);
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "none" : value;
    }

    private static final class Decision {
        private final String phase;
        private final long usedMemory;
        private final long maxMemory;
        private final int usedPercent;
        private final boolean skip;
        private final boolean emergency;

        private Decision(String phase, long usedMemory, long maxMemory, int usedPercent, boolean skip, boolean emergency) {
            this.phase = phase;
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
            this.usedPercent = usedPercent;
            this.skip = skip;
            this.emergency = emergency;
        }
    }
}
