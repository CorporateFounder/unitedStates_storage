package com.example.website.citu.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Logger;

public class UtilUrl {
    public static final int CONNECT_TIMEOUT_MILLIS = 1500;
    public static final int READ_TIMEOUT_MILLIS = 3000;
    private static final Logger LOGGER = Logger.getLogger(UtilUrl.class.getName());
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MILLIS))
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String readJsonFromUrl(String url) throws IOException {
        try {
            HttpURLConnection connection = openConnection(url, "GET");
            try (InputStream is = connection.getInputStream();
                 BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
                return readAll(rd);
            }
        } catch (Exception e) {
            warn("GET", url, e);
            return null;
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String getObject(String jsonObject, String requestStr) throws IOException {
        return requestWithBody(jsonObject, requestStr, "POST");
    }

    public static String getObject(String jsonObject, String requestStr, String requestMethod) throws IOException {
        return requestWithBody(jsonObject, requestStr, requestMethod);
    }

    public static String getObject(String jsonObject, String requestStr, boolean isPost) throws IOException {
        return requestWithBody(jsonObject, requestStr, isPost ? "POST" : "GET");
    }

    public static String getObject(String url) throws IOException {
        try {
            HttpURLConnection conn = openConnection(url, "GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                warn("GET", url, new IOException("HTTP " + responseCode));
                return null;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return readLines(br);
            }
        } catch (Exception e) {
            warn("GET", url, e);
            return null;
        }
    }

    public static int sendPost(String jsonObject, String requestStr) throws IOException {
        try {
            HttpURLConnection conn = openConnection(requestStr, "POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream outputStream = conn.getOutputStream()) {
                byte[] input = bodyBytes(jsonObject);
                outputStream.write(input, 0, input.length);
                return conn.getResponseCode();
            }
        } catch (Exception e) {
            warn("POST", requestStr, e);
            return 0;
        }
    }

    public static String postJson(String url, Object payload) throws IOException, InterruptedException {
        try {
            String json = mapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(READ_TIMEOUT_MILLIS))
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                warn("POST", url, new IOException("HTTP " + response.statusCode()));
                return null;
            }
            return response.body();
        } catch (Exception e) {
            warn("POST", url, e);
            return null;
        }
    }

    public static HttpURLConnection openConnection(String requestUrl, String method) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(READ_TIMEOUT_MILLIS);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        conn.setRequestMethod(method);
        conn.setUseCaches(false);
        return conn;
    }

    private static String requestWithBody(String jsonObject, String requestStr, String requestMethod) {
        String method = requestMethod == null || requestMethod.isBlank() ? "GET" : requestMethod;
        try {
            HttpURLConnection conn = openConnection(requestStr, method);
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");

            if ("GET".equalsIgnoreCase(method)) {
                conn.setDoOutput(false);
            } else {
                conn.setDoOutput(true);
                try (OutputStream outputStream = conn.getOutputStream()) {
                    byte[] input = bodyBytes(jsonObject);
                    outputStream.write(input, 0, input.length);
                    conn.getResponseCode();
                }
            }

            conn.connect();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return readLines(br);
            }
        } catch (Exception e) {
            warn(method, requestStr, e);
            return null;
        }
    }

    private static String readLines(BufferedReader br) throws IOException {
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        return response.toString();
    }

    private static byte[] bodyBytes(String jsonObject) {
        return jsonObject == null ? new byte[0] : jsonObject.getBytes(StandardCharsets.UTF_8);
    }

    private static void warn(String method, String url, Exception e) {
        String type = e instanceof SocketTimeoutException ? "timeout" : e.getClass().getSimpleName();
        String message = e.getMessage() == null ? "" : ": " + e.getMessage();
        LOGGER.warning("UtilUrl " + method + " unavailable " + url + " (" + type + message + ")");
    }
}
