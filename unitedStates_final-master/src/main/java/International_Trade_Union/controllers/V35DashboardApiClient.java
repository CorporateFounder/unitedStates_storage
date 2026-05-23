package International_Trade_Union.controllers;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilUrl;
import International_Trade_Union.utils.UtilsFileSaveRead;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only client for wallet V35 dashboard data.
 */
public class V35DashboardApiClient {
    static final int TIMEOUT_MILLIS = 1500;

    private static final String PROTOCOL_PATH = "/v35/protocol";
    private static final String SUPPLY_PATH = "/v35/supply";
    private static final String BURN_PATH = "/v35/burn";
    private static final String FOUNDER_PATH = "/v35/founder";
    private static final String FOUNDER_BURN_SNAPSHOT_PATH = "/v35/founder/burn-snapshot";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    private final JsonFetcher fetcher;
    private final ServerUrlResolver serverUrlResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public V35DashboardApiClient() {
        this(UtilUrl::readJsonFromUrl, V35DashboardApiClient::configuredServerUrl);
    }

    V35DashboardApiClient(JsonFetcher fetcher, ServerUrlResolver serverUrlResolver) {
        this.fetcher = fetcher;
        this.serverUrlResolver = serverUrlResolver;
    }

    public LiveData fetch() {
        String baseUrl = normalizeBaseUrl(serverUrlResolver.resolve());
        LiveData data = new LiveData(baseUrl);
        if (baseUrl.isEmpty()) {
            data.addError("Live V35 data unavailable: no configured server URL.");
            return data;
        }

        data.protocol = fetchMap(data, baseUrl, PROTOCOL_PATH);
        data.supply = fetchMap(data, baseUrl, SUPPLY_PATH);
        data.burn = fetchMap(data, baseUrl, BURN_PATH);
        data.founder = fetchMap(data, baseUrl, FOUNDER_PATH);
        data.founderBurnSnapshot = fetchMap(data, baseUrl, FOUNDER_BURN_SNAPSHOT_PATH);
        return data;
    }

    private Map<String, Object> fetchMap(LiveData data, String baseUrl, String path) {
        try {
            String json = fetcher.fetch(baseUrl + path, TIMEOUT_MILLIS);
            if (json == null || json.trim().isEmpty()) {
                data.addError(path + " returned empty data.");
                return Collections.emptyMap();
            }
            Map<String, Object> parsed = objectMapper.readValue(json, MAP_TYPE);
            if (parsed == null) {
                data.addError(path + " returned no JSON object.");
                return Collections.emptyMap();
            }
            return parsed;
        } catch (Exception e) {
            data.addError(path + " unavailable.");
            return Collections.emptyMap();
        }
    }

    static String configuredServerUrl() {
        try {
            String savedServer = UtilsFileSaveRead.read(Seting.YOUR_SERVER);
            if (isNotBlank(savedServer)) {
                return savedServer.trim();
            }
        } catch (Exception ignored) {
            // Fall back to bundled server addresses.
        }

        if (Seting.ORIGINAL_ADDRESSES != null) {
            for (String address : Seting.ORIGINAL_ADDRESSES) {
                if (isNotBlank(address)) {
                    return address.trim();
                }
            }
        }
        return "";
    }

    static String normalizeBaseUrl(String value) {
        if (!isNotBlank(value)) {
            return "";
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    static String stringField(Map<String, Object> map, String field, String fallback) {
        if (map == null || !map.containsKey(field)) {
            return fallback;
        }
        Object value = map.get(field);
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    static boolean booleanField(Map<String, Object> map, String field, boolean fallback) {
        if (map == null || !map.containsKey(field)) {
            return fallback;
        }
        Object value = map.get(field);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String text = ((String) value).trim();
            if ("true".equalsIgnoreCase(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text)) {
                return false;
            }
        }
        return fallback;
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> listOfMapsField(Map<String, Object> map, String field) {
        if (map == null || !(map.get(field) instanceof List<?>)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (List<?>) map.get(field)) {
            if (item instanceof Map<?, ?>) {
                Map<String, Object> row = new LinkedHashMap<>();
                ((Map<?, ?>) item).forEach((key, value) -> row.put(String.valueOf(key), value));
                result.add(row);
            }
        }
        return result;
    }

    static List<String> proofChecklistField(Map<String, Object> map, String field) {
        if (map == null) {
            return Collections.emptyList();
        }
        Object value = map.get(field);
        if (value instanceof Map<?, ?>) {
            List<String> result = new ArrayList<>();
            ((Map<?, ?>) value).forEach((key, item) -> result.add(String.valueOf(key) + ": " + String.valueOf(item)));
            return result;
        }
        if (value instanceof List<?>) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return Collections.emptyList();
    }

    static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @FunctionalInterface
    interface JsonFetcher {
        String fetch(String url, int timeoutMillis) throws Exception;
    }

    @FunctionalInterface
    interface ServerUrlResolver {
        String resolve();
    }

    public static class LiveData {
        private final String baseUrl;
        private final List<String> errors = new ArrayList<>();
        private Map<String, Object> protocol = Collections.emptyMap();
        private Map<String, Object> supply = Collections.emptyMap();
        private Map<String, Object> burn = Collections.emptyMap();
        private Map<String, Object> founder = Collections.emptyMap();
        private Map<String, Object> founderBurnSnapshot = Collections.emptyMap();

        public LiveData(String baseUrl) {
            this.baseUrl = baseUrl == null ? "" : baseUrl;
        }

        public static LiveData unavailable(String baseUrl, String error) {
            LiveData data = new LiveData(baseUrl);
            data.addError(error);
            return data;
        }

        public static LiveData of(String baseUrl,
                                  Map<String, Object> protocol,
                                  Map<String, Object> supply,
                                  Map<String, Object> burn,
                                  Map<String, Object> founder,
                                  Map<String, Object> founderBurnSnapshot) {
            LiveData data = new LiveData(baseUrl);
            data.protocol = safeMap(protocol);
            data.supply = safeMap(supply);
            data.burn = safeMap(burn);
            data.founder = safeMap(founder);
            data.founderBurnSnapshot = safeMap(founderBurnSnapshot);
            return data;
        }

        private static Map<String, Object> safeMap(Map<String, Object> map) {
            return map == null ? Collections.emptyMap() : map;
        }

        private void addError(String error) {
            if (isNotBlank(error)) {
                errors.add(error);
            }
        }

        public String baseUrl() {
            return baseUrl;
        }

        public boolean protocolAvailable() {
            return !protocol.isEmpty();
        }

        public boolean supplyAvailable() {
            return !supply.isEmpty();
        }

        public boolean burnAvailable() {
            return !burn.isEmpty();
        }

        public boolean founderAvailable() {
            return !founder.isEmpty();
        }

        public boolean founderBurnSnapshotAvailable() {
            return !founderBurnSnapshot.isEmpty();
        }

        public String errorMessage() {
            if (errors.isEmpty()) {
                return "";
            }
            return "Live V35 data unavailable: " + String.join(" ", errors);
        }

        public Map<String, Object> protocol() {
            return protocol;
        }

        public Map<String, Object> supply() {
            return supply;
        }

        public Map<String, Object> burn() {
            return burn;
        }

        public Map<String, Object> founder() {
            return founder;
        }

        public Map<String, Object> founderBurnSnapshot() {
            return founderBurnSnapshot;
        }
    }
}
