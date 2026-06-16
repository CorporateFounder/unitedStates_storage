package International_Trade_Union.model;

import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.logger.MyLogger;

import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class PeerHealthRegistry {
    private static final ConcurrentHashMap<String, PeerHealth> PEERS = new ConcurrentHashMap<>();
    private static final AtomicLong REMOTE_CALL_WINDOW_START = new AtomicLong(System.currentTimeMillis());
    private static final AtomicInteger REMOTE_CALLS_THIS_MINUTE = new AtomicInteger();
    private static final AtomicInteger PEER_TIMEOUTS_LAST_MINUTE = new AtomicInteger();
    private static final AtomicInteger PEER_SKIPS_LAST_MINUTE = new AtomicInteger();
    private static final AtomicInteger RESOLVE_BUDGET_EXHAUSTED_LAST_MINUTE = new AtomicInteger();

    private static volatile int selectedPeerCountLastCycle;
    private static volatile String lastPeerErrorSummary = "none";
    private static volatile long lastResolve3Duration;
    private static volatile boolean lastResolveBudgetExhausted;
    private static volatile int lastResolvePeerCount;
    private static volatile int lastResolveSelectedPeerCount;
    private static volatile int lastResolveSkippedStalePeerCount;
    private static volatile int lastResolveSkippedQuarantinedPeerCount;
    private static volatile int lastResolveTimeoutCount;
    private static volatile int lastResolveSelfPeerSkippedCount;
    private static volatile long lastPeerSyncDurationMs;
    private static volatile String lastPeerSyncReason = "none";
    private static volatile String lastSlowPeerHost = "none";
    private static volatile long lastSlowPeerDurationMs;
    private static volatile long lastHealthSummaryMillis;

    private PeerHealthRegistry() {
    }

    public static List<HostEndDataShortB> selectHosts(List<HostEndDataShortB> hosts, int maxPeers, String phase, long localSize) {
        if (hosts == null || hosts.isEmpty()) {
            selectedPeerCountLastCycle = 0;
            return new ArrayList<>();
        }
        List<HostEndDataShortB> candidates = new ArrayList<>();
        int selfSkipped = 0;
        int staleSkipped = 0;
        int quarantinedSkipped = 0;
        for (HostEndDataShortB host : hosts) {
            if (host == null || host.getHost() == null || host.getHost().isBlank()) {
                continue;
            }
            String peer = host.getHost();
            long peerSize = peerSize(host);
            if (PeerNetworkPolicy.skipSelfPeers() && isSelfPeer(peer)) {
                selfSkipped++;
                recordSelfSkipped(peer, phase);
                continue;
            }
            if (PeerNetworkPolicy.skipQuarantinedPeers() && isQuarantined(peer)) {
                quarantinedSkipped++;
                recordSkip("PEER_SKIPPED_QUARANTINED", peer, phase);
                continue;
            }
            if (PeerNetworkPolicy.skipStalePeers() && localSize >= 0 && peerSize >= 0
                    && localSize - peerSize >= PeerNetworkPolicy.staleBehindBlocks()) {
                staleSkipped++;
                markStale(peer, localSize, peerSize, phase);
                continue;
            }
            candidates.add(host);
        }
        if (PeerNetworkPolicy.randomizePeerSelection()) {
            Collections.shuffle(candidates);
        }
        if (PeerNetworkPolicy.preferHealthyPeers()) {
            candidates.sort(Comparator.comparingInt(host -> healthRank(host.getHost())));
        }
        int limit = Math.max(1, maxPeers);
        List<HostEndDataShortB> selected = candidates.stream().limit(limit).collect(Collectors.toList());
        selectedPeerCountLastCycle = selected.size();
        if ("resolve3".equals(phase)) {
            lastResolvePeerCount = hosts.size();
            lastResolveSelectedPeerCount = selected.size();
            lastResolveSkippedStalePeerCount = staleSkipped;
            lastResolveSkippedQuarantinedPeerCount = quarantinedSkipped;
            lastResolveSelfPeerSkippedCount = selfSkipped;
        }
        return selected;
    }

    public static List<String> selectPeers(List<String> peers, int maxPeers, String phase, long localSize) {
        if (peers == null || peers.isEmpty()) {
            selectedPeerCountLastCycle = 0;
            return new ArrayList<>();
        }
        List<HostEndDataShortB> hosts = peers.stream()
                .filter(Objects::nonNull)
                .map(peer -> new HostEndDataShortB(peer, null))
                .collect(Collectors.toList());
        return selectHosts(hosts, maxPeers, phase, localSize).stream()
                .map(HostEndDataShortB::getHost)
                .collect(Collectors.toList());
    }

    public static boolean allowRemoteCall(String peer, String endpoint) {
        resetMinuteWindowIfNeeded();
        int count = REMOTE_CALLS_THIS_MINUTE.incrementAndGet();
        if (count > PeerNetworkPolicy.maxRemoteCallsPerMinute()) {
            recordSkip("PEER_REMOTE_CALL_MINUTE_CAP", peer, endpoint);
            return false;
        }
        return true;
    }

    public static void recordSuccess(String peer, String endpoint, long durationMs, long localSize, long peerSize) {
        PeerHealth health = health(peer);
        long now = System.currentTimeMillis();
        synchronized (health) {
            health.lastSeenAt = now;
            health.lastSuccessAt = now;
            health.consecutiveTimeouts = 0;
            health.consecutiveFailures = 0;
            health.lastKnownLocalSize = localSize;
            health.lastKnownPeerSize = peerSize;
            health.behindByBlocks = localSize >= 0 && peerSize >= 0 ? Math.max(0, localSize - peerSize) : -1;
            health.lastResponseDurationMs = durationMs;
            health.lastEndpoint = safe(endpoint);
            health.successCount++;
            if (health.quarantineUntil <= now) {
                health.quarantineUntil = 0;
                health.lastFailureReason = "none";
            }
        }
        if (durationMs > lastSlowPeerDurationMs) {
            lastSlowPeerDurationMs = durationMs;
            lastSlowPeerHost = safe(peer);
        }
    }

    public static void recordFailure(String peer, String endpoint, Throwable error) {
        PeerHealth health = health(peer);
        long now = System.currentTimeMillis();
        boolean timeout = PeerErrorRateLimiter.isNetworkTimeoutLike(error)
                || error instanceof TimeoutException
                || error instanceof SocketTimeoutException;
        synchronized (health) {
            health.lastSeenAt = now;
            health.lastFailureAt = now;
            health.lastEndpoint = safe(endpoint);
            health.lastFailureReason = error == null ? "unknown" : error.getClass().getSimpleName();
            health.failureCount++;
            if (timeout) {
                health.timeoutCount++;
                health.consecutiveTimeouts++;
                PEER_TIMEOUTS_LAST_MINUTE.incrementAndGet();
            } else {
                health.consecutiveFailures++;
            }
            lastPeerErrorSummary = "peer=" + safe(peer)
                    + " endpoint=" + safe(endpoint)
                    + " error=" + health.lastFailureReason
                    + " timeoutCount=" + health.timeoutCount
                    + " failureCount=" + health.failureCount;
            if (timeout && health.consecutiveTimeouts >= PeerNetworkPolicy.maxConsecutiveTimeoutsBeforeQuarantine()) {
                quarantine(health, now, PeerNetworkPolicy.timeoutQuarantineMinutes(), "PEER_QUARANTINED_TIMEOUT");
            } else if (!timeout && health.consecutiveFailures >= PeerNetworkPolicy.maxConsecutiveFailuresBeforeQuarantine()) {
                quarantine(health, now, PeerNetworkPolicy.failureQuarantineMinutes(), "PEER_QUARANTINED_FAILURE");
            }
        }
    }

    public static void markStale(String peer, long localSize, long peerSize, String phase) {
        PeerHealth health = health(peer);
        long now = System.currentTimeMillis();
        synchronized (health) {
            health.lastSeenAt = now;
            health.lastKnownLocalSize = localSize;
            health.lastKnownPeerSize = peerSize;
            health.behindByBlocks = Math.max(0, localSize - peerSize);
            health.lastEndpoint = safe(phase);
            health.lastFailureReason = "stale_or_behind";
            quarantine(health, now, PeerNetworkPolicy.staleQuarantineMinutes(), "PEER_SYNC_SKIPPED_STALE_OR_BEHIND");
        }
    }

    public static void recordSelfSkipped(String peer, String phase) {
        recordSkip("PEER_SKIPPED_SELF", peer, phase);
    }

    public static void recordResolveBudgetExhausted(long durationMs) {
        lastResolveBudgetExhausted = true;
        lastResolve3Duration = durationMs;
        RESOLVE_BUDGET_EXHAUSTED_LAST_MINUTE.incrementAndGet();
        recordSkip("RESOLVE_BUDGET_EXHAUSTED", "resolve3", "budget");
    }

    public static void recordResolveFinished(long durationMs, boolean budgetExhausted) {
        lastResolve3Duration = durationMs;
        lastResolveBudgetExhausted = budgetExhausted;
    }

    public static void recordPeerSync(String reason, long durationMs) {
        lastPeerSyncReason = safe(reason);
        lastPeerSyncDurationMs = durationMs;
    }

    public static boolean isSelfPeer(String peer) {
        return PeerNetworkPolicy.isKnownSelfPeer(peer);
    }

    public static boolean isQuarantined(String peer) {
        PeerHealth health = PEERS.get(PeerNetworkPolicy.normalizePeer(peer));
        return health != null && health.quarantineUntil > System.currentTimeMillis();
    }

    public static Map<String, Object> snapshot() {
        resetMinuteWindowIfNeeded();
        Map<String, Object> status = new LinkedHashMap<>();
        List<PeerHealth> peers = new ArrayList<>(PEERS.values());
        long now = System.currentTimeMillis();
        long quarantined = peers.stream().filter(peer -> peer.quarantineUntil > now).count();
        long stale = peers.stream().filter(peer -> peer.behindByBlocks >= PeerNetworkPolicy.staleBehindBlocks()).count();
        long reachable = peers.stream().filter(peer -> peer.lastSuccessAt > peer.lastFailureAt).count();
        status.put("activePeerCount", peers.size());
        status.put("selectedPeerCountLastCycle", selectedPeerCountLastCycle);
        status.put("reachablePeerCount", reachable);
        status.put("quarantinedPeerCount", quarantined);
        status.put("stalePeerCount", stale);
        status.put("lastPeerErrorSummary", lastPeerErrorSummary);
        status.put("topPeersByFailureCount", topPeers(peers, Comparator.comparingLong((PeerHealth peer) -> peer.failureCount).reversed()));
        status.put("topPeersByResponseDuration", topPeers(peers, Comparator.comparingLong((PeerHealth peer) -> peer.lastResponseDurationMs).reversed()));
        status.put("peersCurrentlyQuarantined", peers.stream().filter(peer -> peer.quarantineUntil > now).map(PeerHealth::toMap).limit(10).collect(Collectors.toList()));
        status.put("peersSkippedAsStale", peers.stream().filter(peer -> peer.behindByBlocks >= PeerNetworkPolicy.staleBehindBlocks()).map(PeerHealth::toMap).limit(10).collect(Collectors.toList()));
        status.put("resolveBudgetExhaustedCount", RESOLVE_BUDGET_EXHAUSTED_LAST_MINUTE.get());
        status.put("remoteCallsLastMinute", REMOTE_CALLS_THIS_MINUTE.get());
        status.put("maxRemoteCallsPerMinute", PeerNetworkPolicy.maxRemoteCallsPerMinute());
        status.put("maxPeersPerCycle", PeerNetworkPolicy.maxPeersPerCycle());
        status.put("maxPeerHealthEntries", PeerNetworkPolicy.maxPeerHealthEntries());
        status.put("maxResolvePeersPerCycle", PeerNetworkPolicy.maxPeersPerResolveCycle());
        status.put("maxPropagationPeers", PeerNetworkPolicy.maxPeersPerPropagationCycle());
        status.putAll(PeerWorkMemoryGuard.snapshot());
        return status;
    }

    public static Map<String, Object> memoryStatusFields() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("lastResolve3Duration", lastResolve3Duration);
        status.put("lastResolveBudgetExhausted", lastResolveBudgetExhausted);
        status.put("lastResolvePeerCount", lastResolvePeerCount);
        status.put("lastResolveSelectedPeerCount", lastResolveSelectedPeerCount);
        status.put("lastResolveSkippedStalePeerCount", lastResolveSkippedStalePeerCount);
        status.put("lastResolveSkippedQuarantinedPeerCount", lastResolveSkippedQuarantinedPeerCount);
        status.put("lastResolveTimeoutCount", lastResolveTimeoutCount);
        status.put("lastResolveSelfPeerSkippedCount", lastResolveSelfPeerSkippedCount);
        status.put("lastPeerSyncDurationMs", lastPeerSyncDurationMs);
        status.put("lastPeerSyncReason", lastPeerSyncReason);
        status.put("lastSlowPeerHost", lastSlowPeerHost);
        status.put("lastSlowPeerDurationMs", lastSlowPeerDurationMs);
        status.put("remoteCallsLastMinute", REMOTE_CALLS_THIS_MINUTE.get());
        status.put("selectedPeerCountLastCycle", selectedPeerCountLastCycle);
        status.put("maxPeersPerCycle", PeerNetworkPolicy.maxPeersPerCycle());
        status.put("maxRemoteCallsPerCycle", PeerNetworkPolicy.maxRemoteCallsPerCycle());
        status.put("maxPeerHealthEntries", PeerNetworkPolicy.maxPeerHealthEntries());
        status.putAll(PeerWorkMemoryGuard.snapshot());
        return status;
    }

    public static void clearTransientPeerBuffers() {
        enforcePeerLimit();
        lastPeerErrorSummary = "cleared_under_memory_pressure";
        lastSlowPeerHost = "none";
        lastSlowPeerDurationMs = 0;
    }

    public static void maybeLogHealthSummary(long chainSize, long latestBlockIndex, long lastTournamentDuration, String lastNoGrowthReason, long lastAddedBlockIndex) {
        long now = System.currentTimeMillis();
        if (now - lastHealthSummaryMillis < 60_000L) {
            return;
        }
        lastHealthSummaryMillis = now;
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> snapshot = snapshot();
        MyLogger.warnLog("CITU_NODE_HEALTH_SUMMARY chainSize=" + chainSize
                + " latestBlockIndex=" + latestBlockIndex
                + " usedMemory=" + (runtime.totalMemory() - runtime.freeMemory())
                + " maxMemory=" + runtime.maxMemory()
                + " activePeerCount=" + snapshot.get("activePeerCount")
                + " selectedPeerCountLastCycle=" + selectedPeerCountLastCycle
                + " reachablePeerCount=" + snapshot.get("reachablePeerCount")
                + " quarantinedPeerCount=" + snapshot.get("quarantinedPeerCount")
                + " stalePeerCount=" + snapshot.get("stalePeerCount")
                + " lastTournamentDuration=" + lastTournamentDuration
                + " lastResolve3Duration=" + lastResolve3Duration
                + " lastNoGrowthReason=" + safe(lastNoGrowthReason)
                + " lastAddedBlockIndex=" + lastAddedBlockIndex
                + " peerTimeoutsLastMinute=" + PEER_TIMEOUTS_LAST_MINUTE.get()
                + " peerSkipsLastMinute=" + PEER_SKIPS_LAST_MINUTE.get()
                + " resolveBudgetExhaustedLastMinute=" + RESOLVE_BUDGET_EXHAUSTED_LAST_MINUTE.get()
                + " remoteCallsLastMinute=" + REMOTE_CALLS_THIS_MINUTE.get()
                + " maxRemoteCallsPerMinute=" + PeerNetworkPolicy.maxRemoteCallsPerMinute());
    }

    public static void resetForTests() {
        PEERS.clear();
        REMOTE_CALL_WINDOW_START.set(System.currentTimeMillis());
        REMOTE_CALLS_THIS_MINUTE.set(0);
        PEER_TIMEOUTS_LAST_MINUTE.set(0);
        PEER_SKIPS_LAST_MINUTE.set(0);
        RESOLVE_BUDGET_EXHAUSTED_LAST_MINUTE.set(0);
        selectedPeerCountLastCycle = 0;
        lastPeerErrorSummary = "none";
        lastResolve3Duration = 0;
        lastResolveBudgetExhausted = false;
        lastResolvePeerCount = 0;
        lastResolveSelectedPeerCount = 0;
        lastResolveSkippedStalePeerCount = 0;
        lastResolveSkippedQuarantinedPeerCount = 0;
        lastResolveTimeoutCount = 0;
        lastResolveSelfPeerSkippedCount = 0;
        lastPeerSyncDurationMs = 0;
        lastPeerSyncReason = "none";
        lastSlowPeerHost = "none";
        lastSlowPeerDurationMs = 0;
        lastHealthSummaryMillis = 0;
        PeerWorkMemoryGuard.resetForTests();
    }

    private static PeerHealth health(String peer) {
        String normalized = PeerNetworkPolicy.normalizePeer(peer);
        PeerHealth health = PEERS.computeIfAbsent(normalized, PeerHealth::new);
        enforcePeerLimit();
        return health;
    }

    private static void enforcePeerLimit() {
        int maxEntries = PeerNetworkPolicy.maxPeerHealthEntries();
        if (PEERS.size() <= maxEntries) {
            return;
        }
        List<PeerHealth> removable = new ArrayList<>(PEERS.values());
        removable.sort(Comparator.comparingLong(PeerHealth::retentionScore));
        int removeCount = Math.max(0, PEERS.size() - maxEntries);
        for (int i = 0; i < removeCount && i < removable.size(); i++) {
            PEERS.remove(removable.get(i).peer, removable.get(i));
        }
    }

    private static void quarantine(PeerHealth health, long now, int minutes, String reason) {
        int boundedMinutes = Math.min(minutes, PeerNetworkPolicy.maxQuarantineMinutes());
        health.quarantineUntil = now + boundedMinutes * 60_000L;
        health.lastFailureReason = reason;
        recordSkip(reason, health.peer, health.lastEndpoint);
    }

    private static int healthRank(String peer) {
        PeerHealth health = PEERS.get(PeerNetworkPolicy.normalizePeer(peer));
        if (health == null) {
            return 1;
        }
        if (health.quarantineUntil > System.currentTimeMillis()) {
            return 99;
        }
        if (health.lastSuccessAt > health.lastFailureAt) {
            return 0;
        }
        return 2;
    }

    private static long peerSize(HostEndDataShortB host) {
        DataShortBlockchainInformation data = host.getDataShortBlockchainInformation();
        return data == null ? -1 : data.getSize();
    }

    private static void recordSkip(String event, String peer, String phase) {
        PEER_SKIPS_LAST_MINUTE.incrementAndGet();
        MyLogger.warnLog(event + " peer=" + safe(peer) + " phase=" + safe(phase));
    }

    private static List<Map<String, Object>> topPeers(List<PeerHealth> peers, Comparator<PeerHealth> comparator) {
        return peers.stream()
                .sorted(comparator)
                .limit(5)
                .map(PeerHealth::toMap)
                .collect(Collectors.toList());
    }

    private static void resetMinuteWindowIfNeeded() {
        long now = System.currentTimeMillis();
        long start = REMOTE_CALL_WINDOW_START.get();
        if (now - start < 60_000L) {
            return;
        }
        if (REMOTE_CALL_WINDOW_START.compareAndSet(start, now)) {
            REMOTE_CALLS_THIS_MINUTE.set(0);
            PEER_TIMEOUTS_LAST_MINUTE.set(0);
            PEER_SKIPS_LAST_MINUTE.set(0);
            RESOLVE_BUDGET_EXHAUSTED_LAST_MINUTE.set(0);
        }
    }

    private static String safe(Object value) {
        if (value == null) {
            return "none";
        }
        String text = String.valueOf(value).replace('\n', ' ').replace('\r', ' ');
        if (text.length() <= 120) {
            return text;
        }
        return text.substring(0, 120) + "...";
    }

    private static final class PeerHealth {
        private final String peer;
        private volatile long lastSeenAt;
        private volatile long lastSuccessAt;
        private volatile long lastFailureAt;
        private volatile int consecutiveTimeouts;
        private volatile int consecutiveFailures;
        private volatile long lastKnownPeerSize = -1;
        private volatile long lastKnownLocalSize = -1;
        private volatile long behindByBlocks = -1;
        private volatile long quarantineUntil;
        private volatile String lastFailureReason = "none";
        private volatile long lastResponseDurationMs;
        private volatile String lastEndpoint = "none";
        private volatile long successCount;
        private volatile long failureCount;
        private volatile long timeoutCount;

        private PeerHealth(String peer) {
            this.peer = peer == null ? "" : peer.toLowerCase(Locale.ROOT);
        }

        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("peer", peer);
            map.put("lastSeenAt", lastSeenAt <= 0 ? "never" : Instant.ofEpochMilli(lastSeenAt).toString());
            map.put("lastSuccessAt", lastSuccessAt <= 0 ? "never" : Instant.ofEpochMilli(lastSuccessAt).toString());
            map.put("lastFailureAt", lastFailureAt <= 0 ? "never" : Instant.ofEpochMilli(lastFailureAt).toString());
            map.put("consecutiveTimeouts", consecutiveTimeouts);
            map.put("consecutiveFailures", consecutiveFailures);
            map.put("lastKnownPeerSize", lastKnownPeerSize);
            map.put("lastKnownLocalSize", lastKnownLocalSize);
            map.put("behindByBlocks", behindByBlocks);
            map.put("quarantineUntil", quarantineUntil <= 0 ? "none" : Instant.ofEpochMilli(quarantineUntil).toString());
            map.put("lastFailureReason", lastFailureReason);
            map.put("lastResponseDurationMs", lastResponseDurationMs);
            map.put("lastEndpoint", lastEndpoint);
            map.put("successCount", successCount);
            map.put("failureCount", failureCount);
            map.put("timeoutCount", timeoutCount);
            return map;
        }

        private long retentionScore() {
            return Math.max(Math.max(lastSeenAt, lastSuccessAt), lastFailureAt);
        }
    }
}
