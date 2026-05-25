package com.example.website.citu.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class V35FounderBurnProofApiClient {
    static final String SUPPLY_PATH = "/v35/supply";
    static final String BURN_PATH = "/v35/burn";
    static final String FOUNDER_PATH = "/v35/founder";
    static final String BURN_SNAPSHOT_PATH = "/v35/founder/burn-snapshot";
    private static final int TIMEOUT_MILLIS = 1500;
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final JsonFetcher fetcher;
    private final BaseUrlResolver baseUrlResolver;
    private final ObjectMapper objectMapper;

    V35FounderBurnProofApiClient() {
        this(V35FounderBurnProofApiClient::httpGet, V35FounderBurnProofApiClient::configuredBaseUrl);
    }

    V35FounderBurnProofApiClient(JsonFetcher fetcher, BaseUrlResolver baseUrlResolver) {
        this.fetcher = fetcher;
        this.baseUrlResolver = baseUrlResolver;
        this.objectMapper = new ObjectMapper();
    }

    LiveStatus fetchStatus() {
        String baseUrl = normalizeBaseUrl(baseUrlResolver.resolve());
        if (baseUrl.isEmpty()) {
            return LiveStatus.unavailable("V35 API base URL is not configured.");
        }

        Map<String, Object> supply = fetchMap(baseUrl, SUPPLY_PATH);
        Map<String, Object> burn = fetchMap(baseUrl, BURN_PATH);
        Map<String, Object> founder = fetchMap(baseUrl, FOUNDER_PATH);
        Map<String, Object> snapshot = fetchMap(baseUrl, BURN_SNAPSHOT_PATH);

        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put(SUPPLY_PATH, endpointStatus(supply));
        statuses.put(BURN_PATH, endpointStatus(burn));
        statuses.put(FOUNDER_PATH, endpointStatus(founder));
        statuses.put(BURN_SNAPSHOT_PATH, endpointStatus(snapshot));

        return new LiveStatus(baseUrl, supply, burn, founder, snapshot, statuses);
    }

    static String text(Map<String, Object> data, String key, String fallback) {
        if (data == null || !data.containsKey(key) || data.get(key) == null) {
            return fallback;
        }
        String value = String.valueOf(data.get(key));
        return value.isBlank() ? fallback : value;
    }

    static boolean flag(Map<String, Object> data, String key, boolean fallback) {
        if (data == null || !data.containsKey(key) || data.get(key) == null) {
            return fallback;
        }
        Object value = data.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Map<String, Object> fetchMap(String baseUrl, String path) {
        try {
            String json = fetcher.fetch(baseUrl + path, TIMEOUT_MILLIS);
            if (json == null || json.isBlank()) {
                return error("Empty response");
            }
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return error(e.getClass().getSimpleName() + ": " + safeMessage(e));
        }
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("_error", message);
        return error;
    }

    private static String endpointStatus(Map<String, Object> data) {
        return data == null || data.containsKey("_error") ? "Unavailable" : "Available";
    }

    private static String safeMessage(Exception e) {
        return e.getMessage() == null ? "request failed" : e.getMessage();
    }

    private static String configuredBaseUrl() {
        String configured = System.getProperty("citu.v35.api.base-url", "");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("CITU_V35_API_BASE_URL");
        }
        if (configured == null || configured.isBlank()) {
            configured = ExplorerController.getAddress();
        }
        return configured;
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String httpGet(String url, int timeoutMillis) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setUseCaches(false);
        connection.setRequestProperty("Accept", "application/json");

        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code);
        }
        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    interface JsonFetcher {
        String fetch(String url, int timeoutMillis) throws Exception;
    }

    interface BaseUrlResolver {
        String resolve();
    }

    record LiveStatus(
            String baseUrl,
            Map<String, Object> supply,
            Map<String, Object> burn,
            Map<String, Object> founder,
            Map<String, Object> snapshot,
            Map<String, String> endpointStatuses
    ) {
        static LiveStatus unavailable(String reason) {
            Map<String, Object> error = error(reason);
            Map<String, String> statuses = new LinkedHashMap<>();
            statuses.put(SUPPLY_PATH, "Unavailable");
            statuses.put(BURN_PATH, "Unavailable");
            statuses.put(FOUNDER_PATH, "Unavailable");
            statuses.put(BURN_SNAPSHOT_PATH, "Unavailable");
            return new LiveStatus("", error, error, error, error, statuses);
        }

        boolean anyEndpointAvailable() {
            return endpointStatuses.values().stream().anyMatch("Available"::equals);
        }

        String overallStatus() {
            return anyEndpointAvailable() ? "Partial / available" : "Unavailable";
        }

        String warning() {
            if (anyEndpointAvailable()) {
                return "Live V35 API data is optional. Pending transaction proof fields remain pending until real burn proof is published.";
            }
            return "Live V35 data unavailable. Static proof template remains visible.";
        }

        Map<String, Object> safeSupply() {
            return supply == null ? Collections.emptyMap() : supply;
        }

        Map<String, Object> safeBurn() {
            return burn == null ? Collections.emptyMap() : burn;
        }

        Map<String, Object> safeFounder() {
            return founder == null ? Collections.emptyMap() : founder;
        }

        Map<String, Object> safeSnapshot() {
            return snapshot == null ? Collections.emptyMap() : snapshot;
        }
    }
}
