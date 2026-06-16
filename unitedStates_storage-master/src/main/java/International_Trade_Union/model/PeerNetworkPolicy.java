package International_Trade_Union.model;

import java.util.Locale;

public final class PeerNetworkPolicy {
    public static final int DEFAULT_MAX_PEERS_PER_CYCLE = 3;
    public static final int DEFAULT_MAX_REMOTE_CALLS_PER_CYCLE = 6;
    public static final int DEFAULT_MAX_REMOTE_CALLS_PER_MINUTE = 60;
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 2000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 3000;
    public static final int DEFAULT_RESOLVE_MAX_DURATION_MS = 5000;
    public static final int DEFAULT_TOURNAMENT_PEER_BUDGET_MS = 3000;
    public static final int DEFAULT_STALE_BEHIND_BLOCKS = 1000;
    public static final int DEFAULT_MAX_PEER_HEALTH_ENTRIES = 32;
    public static final int DEFAULT_MAX_PEER_ERROR_ENTRIES = 32;
    public static final int DEFAULT_MAX_BLOCKED_HOST_ENTRIES = 32;

    private PeerNetworkPolicy() {
    }

    public static int maxPeersPerCycle() {
        return positiveInt("citu.peer.maxPeersPerCycle", DEFAULT_MAX_PEERS_PER_CYCLE);
    }

    public static int maxPeersPerTournamentCycle() {
        return positiveInt("citu.peer.maxPeersPerTournamentCycle", maxPeersPerCycle());
    }

    public static int maxPeersPerResolveCycle() {
        return positiveInt("citu.peer.maxPeersPerResolveCycle", maxPeersPerCycle());
    }

    public static int maxPeersPerPropagationCycle() {
        return positiveInt("citu.peer.maxPeersPerPropagationCycle", maxPeersPerCycle());
    }

    public static int maxPeersPerReadinessCycle() {
        return positiveInt("citu.peer.maxPeersPerReadinessCycle", maxPeersPerCycle());
    }

    public static int maxRemoteCallsPerCycle() {
        return positiveInt("citu.peer.maxRemoteCallsPerCycle", DEFAULT_MAX_REMOTE_CALLS_PER_CYCLE);
    }

    public static int maxRemoteCallsPerMinute() {
        return positiveInt("citu.peer.maxRemoteCallsPerMinute", DEFAULT_MAX_REMOTE_CALLS_PER_MINUTE);
    }

    public static int maxRemoteCallDurationMs() {
        return positiveInt("citu.peer.maxRemoteCallDurationMs", DEFAULT_READ_TIMEOUT_MS);
    }

    public static int connectTimeoutMs() {
        return positiveInt("citu.peer.connectTimeoutMs", DEFAULT_CONNECT_TIMEOUT_MS);
    }

    public static int readTimeoutMs() {
        return positiveInt("citu.peer.readTimeoutMs", DEFAULT_READ_TIMEOUT_MS);
    }

    public static int resolveMaxDurationMs() {
        return positiveInt("citu.resolve.maxDurationMs", DEFAULT_RESOLVE_MAX_DURATION_MS);
    }

    public static int tournamentPeerBudgetMs() {
        return positiveInt("citu.tournament.peerBudgetMs", DEFAULT_TOURNAMENT_PEER_BUDGET_MS);
    }

    public static int tournamentMaxPeerCalls() {
        return positiveInt("citu.tournament.maxPeerCalls", maxPeersPerTournamentCycle());
    }

    public static int staleBehindBlocks() {
        return positiveInt("citu.peer.staleBehindBlocks", DEFAULT_STALE_BEHIND_BLOCKS);
    }

    public static int staleQuarantineMinutes() {
        return positiveInt("citu.peer.staleQuarantineMinutes", 30);
    }

    public static int timeoutQuarantineMinutes() {
        return positiveInt("citu.peer.timeoutQuarantineMinutes", 10);
    }

    public static int failureQuarantineMinutes() {
        return positiveInt("citu.peer.failureQuarantineMinutes", 15);
    }

    public static int maxQuarantineMinutes() {
        return positiveInt("citu.peer.maxQuarantineMinutes", 60);
    }

    public static int maxConsecutiveTimeoutsBeforeQuarantine() {
        return positiveInt("citu.peer.maxConsecutiveTimeoutsBeforeQuarantine", 2);
    }

    public static int maxConsecutiveFailuresBeforeQuarantine() {
        return positiveInt("citu.peer.maxConsecutiveFailuresBeforeQuarantine", 3);
    }

    public static int maxPeerHealthEntries() {
        return positiveInt("citu.peer.maxHealthEntries", DEFAULT_MAX_PEER_HEALTH_ENTRIES);
    }

    public static int maxPeerErrorEntries() {
        return positiveInt("citu.peer.maxErrorEntries", DEFAULT_MAX_PEER_ERROR_ENTRIES);
    }

    public static int maxBlockedHostEntries() {
        return positiveInt("citu.peer.maxBlockedHostEntries", DEFAULT_MAX_BLOCKED_HOST_ENTRIES);
    }

    public static int memoryGuardSkipPercent() {
        return boundedPercent("citu.memoryGuard.skipPeerWorkPercent", 70);
    }

    public static int memoryGuardEmergencyPercent() {
        return boundedPercent("citu.memoryGuard.emergencyPeerWorkPercent", 85);
    }

    public static boolean randomizePeerSelection() {
        return bool("citu.peer.randomizePeerSelection", true);
    }

    public static boolean preferHealthyPeers() {
        return bool("citu.peer.preferHealthyPeers", true);
    }

    public static boolean skipQuarantinedPeers() {
        return bool("citu.peer.skipQuarantinedPeers", true);
    }

    public static boolean skipSelfPeers() {
        return bool("citu.peer.skipSelfPeers", true);
    }

    public static boolean skipStalePeers() {
        return bool("citu.peer.skipStalePeers", true);
    }

    public static String normalizePeer(String peer) {
        if (peer == null) {
            return "";
        }
        String normalized = peer.trim().toLowerCase(Locale.ROOT);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public static boolean isKnownSelfPeer(String peer) {
        String normalized = normalizePeer(peer);
        return normalized.equals("http://194.87.236.238:82")
                || normalized.equals("https://194.87.236.238:82")
                || normalized.equals("194.87.236.238:82")
                || normalized.equals("http://localhost:82")
                || normalized.equals("https://localhost:82")
                || normalized.equals("localhost:82")
                || normalized.equals("http://127.0.0.1:82")
                || normalized.equals("https://127.0.0.1:82")
                || normalized.equals("127.0.0.1:82");
    }

    private static int positiveInt(String key, int defaultValue) {
        return Math.max(1, Integer.getInteger(key, defaultValue));
    }

    private static int boundedPercent(String key, int defaultValue) {
        return Math.max(1, Math.min(99, Integer.getInteger(key, defaultValue)));
    }

    private static boolean bool(String key, boolean defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}
