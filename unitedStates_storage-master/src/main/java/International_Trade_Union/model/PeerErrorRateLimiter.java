package International_Trade_Union.model;

import International_Trade_Union.logger.MyLogger;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

public class PeerErrorRateLimiter {
    static final long DEFAULT_SUMMARY_INTERVAL_MILLIS = 300_000L;
    static final long SUMMARY_COUNT_INTERVAL = 100L;

    private final long summaryIntervalMillis;
    private final Map<String, PeerErrorState> states = new ConcurrentHashMap<>();

    public PeerErrorRateLimiter() {
        this(DEFAULT_SUMMARY_INTERVAL_MILLIS);
    }

    PeerErrorRateLimiter(long summaryIntervalMillis) {
        this.summaryIntervalMillis = Math.max(1, summaryIntervalMillis);
    }

    private static final boolean STACKTRACE_FOR_NETWORK_ERRORS = Boolean.getBoolean("citu.peer.error.stacktrace.enabled");

    public void logPeerError(String peer, Exception error) {
        logPeerError(peer, "unknown", error);
    }

    public void logPeerError(String peer, String endpoint, Throwable error) {
        String normalizedPeer = peer == null || peer.isBlank() ? "unknown" : peer;
        String normalizedEndpoint = endpoint == null || endpoint.isBlank() ? "unknown" : endpoint;
        Throwable root = rootCause(error);
        String errorClass = root == null ? "unknown" : root.getClass().getSimpleName();
        String errorMessage = root == null ? "unknown" : root.getMessage();
        if (errorMessage == null || errorMessage.isBlank()) {
            errorMessage = errorClass;
        }
        String key = normalizedPeer + "|" + normalizedEndpoint + "|" + errorClass + "|" + errorMessage;
        long now = System.currentTimeMillis();
        PeerErrorState state = states.computeIfAbsent(key, ignored -> new PeerErrorState());
        long count = state.increment(now);
        enforceStateLimit();
        String summary = "peer=" + normalizedPeer
                + ", endpoint=" + normalizedEndpoint
                + ", error=" + errorClass
                + ", message=" + errorMessage
                + ", count=" + count
                + ", lastSeen=" + new Date(now);

        if (count == 1) {
            if (isNetworkTimeoutLike(error)) {
                if (STACKTRACE_FOR_NETWORK_ERRORS) {
                    MyLogger.warnLog(summary, root);
                } else {
                    MyLogger.warnLog(summary);
                }
            } else {
                MyLogger.saveLog(summary, root);
            }
            return;
        }

        if (state.shouldLogSummary(now, summaryIntervalMillis, SUMMARY_COUNT_INTERVAL)) {
            MyLogger.warnLog(summary);
        }
    }

    public static boolean isNetworkTimeoutLike(Throwable error) {
        Throwable root = rootCause(error);
        if (root instanceof TimeoutException
                || root instanceof SocketTimeoutException
                || root instanceof ConnectException
                || root instanceof NoRouteToHostException
                || root instanceof UnknownHostException) {
            return true;
        }
        String message = root == null ? null : root.getMessage();
        if (message == null) {
            message = error == null ? null : error.getMessage();
        }
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("timed out")
                || normalized.contains("timeout")
                || normalized.contains("connection refused")
                || normalized.contains("connection reset")
                || normalized.contains("no route to host")
                || normalized.contains("unknown host");
    }

    static Throwable rootCause(Throwable error) {
        Throwable current = error;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        while (current != null && current.getCause() != null && current != current.getCause()) {
            current = current.getCause();
        }
        return current;
    }

    public int trackedErrorCount() {
        return states.size();
    }

    public String lastWarning() {
        return states.entrySet().stream()
                .max((left, right) -> Long.compare(left.getValue().lastSeenMillis, right.getValue().lastSeenMillis))
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", 4);
                    String peer = parts.length > 0 ? parts[0] : "unknown";
                    String endpoint = parts.length > 1 ? parts[1] : "unknown";
                    String error = parts.length > 2 ? parts[2] : "unknown";
                    PeerErrorState state = entry.getValue();
                    return "peer=" + peer + ", endpoint=" + endpoint + ", error=" + error + ", count=" + state.count + ", lastSeen=" + new Date(state.lastSeenMillis);
                })
                .orElse("none");
    }

    void resetForTests() {
        states.clear();
    }

    private void enforceStateLimit() {
        int maxEntries = PeerNetworkPolicy.maxPeerErrorEntries();
        if (states.size() <= maxEntries) {
            return;
        }
        List<Map.Entry<String, PeerErrorState>> removable = new ArrayList<>(states.entrySet());
        removable.sort(Comparator.comparingLong(entry -> entry.getValue().lastSeenMillis));
        int removeCount = states.size() - maxEntries;
        for (int i = 0; i < removeCount && i < removable.size(); i++) {
            states.remove(removable.get(i).getKey(), removable.get(i).getValue());
        }
    }

    private static class PeerErrorState {
        private long count;
        private long lastSeenMillis;
        private long lastSummaryMillis;

        synchronized long increment(long now) {
            count++;
            lastSeenMillis = now;
            if (count == 1) {
                lastSummaryMillis = now;
            }
            return count;
        }

        synchronized boolean shouldLogSummary(long now, long intervalMillis, long countInterval) {
            if (countInterval > 0 && count % countInterval == 0) {
                lastSummaryMillis = now;
                return true;
            }
            if (now - lastSummaryMillis < intervalMillis) {
                return false;
            }
            lastSummaryMillis = now;
            return true;
        }
    }
}
