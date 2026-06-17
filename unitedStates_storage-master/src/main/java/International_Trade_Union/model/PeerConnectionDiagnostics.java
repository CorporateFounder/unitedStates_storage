package International_Trade_Union.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class PeerConnectionDiagnostics {
    private static final ConcurrentHashMap<String, AtomicInteger> ACTIVE_REQUESTS_BY_ENDPOINT = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> ACTIVE_FUTURES_BY_PHASE = new ConcurrentHashMap<>();

    private PeerConnectionDiagnostics() {
    }

    public static void beginRequest(String endpoint) {
        ACTIVE_REQUESTS_BY_ENDPOINT.computeIfAbsent(safeKey(endpoint), ignored -> new AtomicInteger()).incrementAndGet();
    }

    public static void endRequest(String endpoint) {
        decrement(ACTIVE_REQUESTS_BY_ENDPOINT, safeKey(endpoint));
    }

    public static void beginFutures(String phase, int count) {
        if (count <= 0) {
            return;
        }
        ACTIVE_FUTURES_BY_PHASE.computeIfAbsent(safeKey(phase), ignored -> new AtomicInteger()).addAndGet(count);
    }

    public static void endFutures(String phase, int count) {
        if (count <= 0) {
            return;
        }
        String key = safeKey(phase);
        AtomicInteger value = ACTIVE_FUTURES_BY_PHASE.get(key);
        if (value == null) {
            return;
        }
        int current = value.addAndGet(-count);
        if (current <= 0) {
            ACTIVE_FUTURES_BY_PHASE.remove(key, value);
        }
    }

    public static Map<String, Object> snapshot() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("activeRequestsByEndpoint", copy(ACTIVE_REQUESTS_BY_ENDPOINT));
        status.put("activeFuturesByPhase", copy(ACTIVE_FUTURES_BY_PHASE));
        status.put("activePeerTasksCount", ACTIVE_REQUESTS_BY_ENDPOINT.values().stream().mapToInt(AtomicInteger::get).sum());
        status.put("activeTournamentFuturesCount", ACTIVE_FUTURES_BY_PHASE.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains("tournament")
                        || entry.getKey().toLowerCase().contains("winner")
                        || entry.getKey().toLowerCase().contains("sync"))
                .mapToInt(entry -> entry.getValue().get())
                .sum());
        status.put("activeResolveFuturesCount", ACTIVE_FUTURES_BY_PHASE.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains("resolve")
                        || entry.getKey().toLowerCase().contains("fetchdata"))
                .mapToInt(entry -> entry.getValue().get())
                .sum());
        return status;
    }

    public static void resetForTests() {
        ACTIVE_REQUESTS_BY_ENDPOINT.clear();
        ACTIVE_FUTURES_BY_PHASE.clear();
    }

    private static Map<String, Integer> copy(ConcurrentHashMap<String, AtomicInteger> source) {
        Map<String, Integer> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            int count = value.get();
            if (count > 0) {
                result.put(key, count);
            }
        });
        return result;
    }

    private static void decrement(ConcurrentHashMap<String, AtomicInteger> source, String key) {
        AtomicInteger value = source.get(key);
        if (value == null) {
            return;
        }
        int current = value.decrementAndGet();
        if (current <= 0) {
            source.remove(key, value);
        }
    }

    private static String safeKey(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
