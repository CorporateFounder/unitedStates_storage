package International_Trade_Union.utils;

import International_Trade_Union.model.NodeState;
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
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 3000;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 6000;
    public static final int SILENT_TIMEOUT_MILLIS = 3000;
    public static final int MAX_GET_RESPONSE_CHARS = 1_000_000;

    public static String readJsonFromUrl_silent(String url) throws IOException, JSONException {
        URLConnection conn = openConnection(url, SILENT_TIMEOUT_MILLIS, SILENT_TIMEOUT_MILLIS);
        try (InputStream is = conn.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
            return readAll(rd, MAX_GET_RESPONSE_CHARS);
        } finally {
            disconnect(conn);
        }
    }

    public static String readJsonFromUrl(String url, int timeoutMillis) throws IOException, JSONException {
        URLConnection conn = openConnection(url, timeoutMillis, timeoutMillis);
        try (InputStream is = conn.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
            return readAll(rd, MAX_GET_RESPONSE_CHARS);
        } finally {
            disconnect(conn);
        }
    }

    public static String readJsonFromUrl(String url) throws IOException, JSONException {
        URLConnection conn = openConnection(url, DEFAULT_CONNECT_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS);
        try (InputStream is = conn.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
            return readAll(rd, MAX_GET_RESPONSE_CHARS);
        } finally {
            disconnect(conn);
        }
    }

    static String readAll(Reader rd, int maxChars) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            if (sb.length() >= maxChars) {
                throw new IOException("Response is too large");
            }
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String getObject(String jsonObject, String requstStr) throws IOException {
        HttpURLConnection conn = openHttpConnection(
                requstStr,
                "POST",
                DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DEFAULT_READ_TIMEOUT_MILLIS
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
                return response.toString();
            }
        } finally {
            conn.disconnect();
        }
    }

    public static int sendPost(String jsonObject, String requestStr) throws IOException {
        HttpURLConnection conn = openHttpConnection(
                requestStr,
                "POST",
                DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DEFAULT_READ_TIMEOUT_MILLIS
        );
        try {
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream outputStream = conn.getOutputStream()) {
                byte[] input = jsonObject.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
                return conn.getResponseCode();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            conn.disconnect();
        }
    }

    public static int sendJsonToUrl(String requestStr, NodeState state, int timeoutMillis) throws IOException {
        HttpURLConnection conn = openHttpConnection(requestStr, "POST", timeoutMillis, timeoutMillis);
        try {
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonObject = UtilsJson.objToStringJson(state);
            try (OutputStream outputStream = conn.getOutputStream()) {
                byte[] input = jsonObject.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
                return conn.getResponseCode();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
}
