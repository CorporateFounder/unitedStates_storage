package International_Trade_Union.utils;

import International_Trade_Union.model.NodeMemoryProfiler;
import International_Trade_Union.model.NodeState;
import International_Trade_Union.model.PeerHealthRegistry;
import International_Trade_Union.model.PeerNetworkPolicy;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class UtilUrl {
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = PeerNetworkPolicy.connectTimeoutMs();
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = PeerNetworkPolicy.readTimeoutMs();
    public static final int SILENT_TIMEOUT_MILLIS = PeerNetworkPolicy.readTimeoutMs();
    public static final int MAX_GET_RESPONSE_CHARS = 1_000_000;
    public static final int MAX_WINNER_LIST_RESPONSE_CHARS = 200_000;
    public static final int MAX_GET_NODES_RESPONSE_CHARS = 100_000;
    public static final int MAX_GET_TRANSACTIONS_RESPONSE_CHARS = 500_000;
    public static final int MAX_SUB_BLOCKS_RESPONSE_CHARS = 750_000;
    public static final int MAX_DATASHORT_RESPONSE_CHARS = 100_000;
    public static final String TOO_LARGE_REASON = "PEER_RESPONSE_SKIPPED_TOO_LARGE";

    public static String readJsonFromUrl_silent(String url) throws IOException, JSONException {
        return readJsonFromUrl(url, SILENT_TIMEOUT_MILLIS, MAX_GET_RESPONSE_CHARS);
    }

    public static String readJsonFromUrl(String url, int timeoutMillis) throws IOException, JSONException {
        return readJsonFromUrl(url, timeoutMillis, maxResponseCharsForUrl(url));
    }

    public static String readJsonFromUrl(String url, int timeoutMillis, int maxChars) throws IOException, JSONException {
        long start = System.currentTimeMillis();
        String peer = peerFromUrl(url);
        String endpoint = endpointFromUrl(url);
        if (!PeerHealthRegistry.allowRemoteCall(peer, endpoint)) {
            throw new IOException("PEER_REMOTE_CALL_LIMIT_EXCEEDED endpoint=" + endpoint);
        }
        NodeMemoryProfiler.recordStage("UtilUrl before remote GET", NodeMemoryProfiler.metrics(
                "url", url,
                "timeoutMillis", timeoutMillis,
                "maxChars", maxChars
        ));
        int boundedTimeout = boundedTimeout(timeoutMillis);
        URLConnection conn = openConnection(url, Math.min(PeerNetworkPolicy.connectTimeoutMs(), boundedTimeout), Math.min(PeerNetworkPolicy.readTimeoutMs(), boundedTimeout));
        try (InputStream is = conn.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
            String response = readAll(rd, maxChars);
            PeerHealthRegistry.recordSuccess(peer, endpoint, System.currentTimeMillis() - start, -1, -1);
            NodeMemoryProfiler.recordStage("UtilUrl after remote GET", NodeMemoryProfiler.metrics(
                    "url", url,
                    "responseBytes", response == null ? 0 : response.length(),
                    "jsonStringLength", response == null ? 0 : response.length(),
                    "durationMs", System.currentTimeMillis() - start,
                    "maxChars", maxChars
            ));
            return response;
        } catch (IOException e) {
            PeerHealthRegistry.recordFailure(peer, endpoint, e);
            NodeMemoryProfiler.recordStage("UtilUrl remote GET failure", NodeMemoryProfiler.metrics(
                    "url", url,
                    "exception", e.getClass().getSimpleName(),
                    "durationMs", System.currentTimeMillis() - start,
                    "maxChars", maxChars
            ));
            throw e;
        } finally {
            disconnect(conn);
        }
    }

    public static String readJsonFromUrl(String url) throws IOException, JSONException {
        return readJsonFromUrl(url, DEFAULT_READ_TIMEOUT_MILLIS, maxResponseCharsForUrl(url));
    }

    static String readAll(Reader rd, int maxChars) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            if (sb.length() >= maxChars) {
                NodeMemoryProfiler.recordStage("UtilUrl remote response skipped", NodeMemoryProfiler.metrics(
                        "skipReason", TOO_LARGE_REASON,
                        "responseChars", sb.length(),
                        "capChars", maxChars
                ));
                throw new IOException(TOO_LARGE_REASON + ": capChars=" + maxChars);
            }
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static int maxResponseCharsForUrl(String url) {
        String normalized = url == null ? "" : url.toLowerCase();
        if (normalized.contains("/winnerlist")) {
            return Integer.getInteger("citu.peer.maxWinnerListBytes", MAX_WINNER_LIST_RESPONSE_CHARS);
        }
        if (normalized.contains("/gettransactions")) {
            return Integer.getInteger("citu.peer.maxTransactionsBytes", MAX_GET_TRANSACTIONS_RESPONSE_CHARS);
        }
        if (normalized.contains("/sub-blocks") || normalized.contains("/subblocks")) {
            return Integer.getInteger("citu.peer.maxSubBlocksBytes", MAX_SUB_BLOCKS_RESPONSE_CHARS);
        }
        if (normalized.contains("/datashort")) {
            return Integer.getInteger("citu.peer.maxDataShortBytes", MAX_DATASHORT_RESPONSE_CHARS);
        }
        return Integer.getInteger("citu.peer.maxResponseBytes.default", MAX_GET_RESPONSE_CHARS);
    }

    public static String getObject(String jsonObject, String requstStr) throws IOException {
        long start = System.currentTimeMillis();
        String peer = peerFromUrl(requstStr);
        String endpoint = endpointFromUrl(requstStr);
        if (!PeerHealthRegistry.allowRemoteCall(peer, endpoint)) {
            throw new IOException("PEER_REMOTE_CALL_LIMIT_EXCEEDED endpoint=" + endpoint);
        }
        NodeMemoryProfiler.recordStage("UtilUrl before remote POST", NodeMemoryProfiler.metrics(
                "url", requstStr,
                "requestBytes", jsonObject == null ? 0 : jsonObject.length(),
                "method", "POST"
        ));
        HttpURLConnection conn = openHttpConnection(
                requstStr,
                "POST",
                PeerNetworkPolicy.connectTimeoutMs(),
                PeerNetworkPolicy.readTimeoutMs()
        );
        try {
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream outputStream = conn.getOutputStream()) {
                byte[] input = jsonObject.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
                conn.getResponseCode();
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                String result = response.toString();
                PeerHealthRegistry.recordSuccess(peer, endpoint, System.currentTimeMillis() - start, -1, -1);
                NodeMemoryProfiler.recordStage("UtilUrl after remote POST", NodeMemoryProfiler.metrics(
                        "url", requstStr,
                        "responseBytes", result.length(),
                        "durationMs", System.currentTimeMillis() - start,
                        "method", "POST"
                ));
                return result;
            }
        } catch (IOException e) {
            PeerHealthRegistry.recordFailure(peer, endpoint, e);
            NodeMemoryProfiler.recordStage("UtilUrl remote POST failure", NodeMemoryProfiler.metrics(
                    "url", requstStr,
                    "exception", e.getClass().getSimpleName(),
                    "durationMs", System.currentTimeMillis() - start,
                    "method", "POST"
            ));
            throw e;
        } finally {
            conn.disconnect();
        }
    }

    public static int sendPost(String jsonObject, String requestStr) throws IOException {
        return sendPost(jsonObject, requestStr, DEFAULT_READ_TIMEOUT_MILLIS);
    }

    public static int sendPost(String jsonObject, String requestStr, int timeoutMillis) throws IOException {
        long start = System.currentTimeMillis();
        String peer = peerFromUrl(requestStr);
        String endpoint = endpointFromUrl(requestStr);
        if (!PeerHealthRegistry.allowRemoteCall(peer, endpoint)) {
            throw new IOException("PEER_REMOTE_CALL_LIMIT_EXCEEDED endpoint=" + endpoint);
        }
        NodeMemoryProfiler.recordStage("UtilUrl before sendPost", NodeMemoryProfiler.metrics(
                "url", requestStr,
                "requestBytes", jsonObject == null ? 0 : jsonObject.length(),
                "timeoutMillis", timeoutMillis
        ));
        HttpURLConnection conn = openHttpConnection(
                requestStr,
                "POST",
                Math.min(PeerNetworkPolicy.connectTimeoutMs(), boundedTimeout(timeoutMillis)),
                Math.min(PeerNetworkPolicy.readTimeoutMs(), boundedTimeout(timeoutMillis))
        );
        try {
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream outputStream = conn.getOutputStream()) {
                byte[] input = jsonObject.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
                int responseCode = conn.getResponseCode();
                PeerHealthRegistry.recordSuccess(peer, endpoint, System.currentTimeMillis() - start, -1, -1);
                NodeMemoryProfiler.recordStage("UtilUrl after sendPost", NodeMemoryProfiler.metrics(
                        "url", requestStr,
                        "requestBytes", input.length,
                        "statusCode", responseCode,
                        "durationMs", System.currentTimeMillis() - start
                ));
                return responseCode;
            }
        } catch (IOException e) {
            PeerHealthRegistry.recordFailure(peer, endpoint, e);
            NodeMemoryProfiler.recordStage("UtilUrl sendPost failure", NodeMemoryProfiler.metrics(
                    "url", requestStr,
                    "exception", e.getClass().getSimpleName(),
                    "durationMs", System.currentTimeMillis() - start
            ));
            throw e;
        } finally {
            conn.disconnect();
        }
    }

    public static int sendJsonToUrl(String requestStr, NodeState state, int timeoutMillis) throws IOException {
        long start = System.currentTimeMillis();
        String peer = peerFromUrl(requestStr);
        String endpoint = endpointFromUrl(requestStr);
        if (!PeerHealthRegistry.allowRemoteCall(peer, endpoint)) {
            throw new IOException("PEER_REMOTE_CALL_LIMIT_EXCEEDED endpoint=" + endpoint);
        }
        int boundedTimeout = boundedTimeout(timeoutMillis);
        HttpURLConnection conn = openHttpConnection(requestStr, "POST", Math.min(PeerNetworkPolicy.connectTimeoutMs(), boundedTimeout), Math.min(PeerNetworkPolicy.readTimeoutMs(), boundedTimeout));
        try {
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonObject = UtilsJson.objToStringJson(state);
            NodeMemoryProfiler.recordStage("UtilUrl before sendJsonToUrl", NodeMemoryProfiler.metrics(
                    "url", requestStr,
                    "requestBytes", jsonObject == null ? 0 : jsonObject.length(),
                    "timeoutMillis", timeoutMillis
            ));
            try (OutputStream outputStream = conn.getOutputStream()) {
                byte[] input = jsonObject.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
                int responseCode = conn.getResponseCode();
                PeerHealthRegistry.recordSuccess(peer, endpoint, System.currentTimeMillis() - start, -1, -1);
                NodeMemoryProfiler.recordStage("UtilUrl after sendJsonToUrl", NodeMemoryProfiler.metrics(
                        "url", requestStr,
                        "requestBytes", input.length,
                        "statusCode", responseCode,
                        "durationMs", System.currentTimeMillis() - start
                ));
                return responseCode;
            }
        } catch (IOException e) {
            PeerHealthRegistry.recordFailure(peer, endpoint, e);
            NodeMemoryProfiler.recordStage("UtilUrl sendJsonToUrl failure", NodeMemoryProfiler.metrics(
                    "url", requestStr,
                    "exception", e.getClass().getSimpleName(),
                    "durationMs", System.currentTimeMillis() - start
            ));
            throw e;
        } finally {
            conn.disconnect();
        }
    }

    public static URLConnection openConnection(String requestUrl, int connectTimeoutMillis, int readTimeoutMillis) throws IOException {
        URLConnection conn = new URL(requestUrl).openConnection();
        conn.setConnectTimeout(connectTimeoutMillis);
        conn.setReadTimeout(readTimeoutMillis);
        return conn;
    }

    public static HttpURLConnection openHttpConnection(
            String requestUrl,
            String method,
            int connectTimeoutMillis,
            int readTimeoutMillis
    ) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) openConnection(requestUrl, connectTimeoutMillis, readTimeoutMillis);
        conn.setRequestMethod(method);
        return conn;
    }

    private static void disconnect(URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

    private static int boundedTimeout(int timeoutMillis) {
        return Math.max(1, Math.min(timeoutMillis, PeerNetworkPolicy.maxRemoteCallDurationMs()));
    }

    private static String peerFromUrl(String requestUrl) {
        if (requestUrl == null || requestUrl.isBlank()) {
            return "unknown";
        }
        try {
            URL url = new URL(requestUrl);
            int port = url.getPort();
            String authority = url.getProtocol() + "://" + url.getHost() + (port < 0 ? "" : ":" + port);
            return authority;
        } catch (Exception ignored) {
            return requestUrl;
        }
    }

    private static String endpointFromUrl(String requestUrl) {
        if (requestUrl == null || requestUrl.isBlank()) {
            return "unknown";
        }
        try {
            URL url = new URL(requestUrl);
            return url.getPath() == null || url.getPath().isBlank() ? "/" : url.getPath();
        } catch (Exception ignored) {
            return "unknown";
        }
    }
}
