package International_Trade_Union.utils;

import International_Trade_Union.model.NodeMemoryProfiler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilUrlForensicsTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("citu.memoryForensics.enabled");
        System.clearProperty("citu.memoryForensics.lastTraces");
        System.clearProperty("citu.memoryForensics.file.enabled");
        NodeMemoryProfiler.clearForTests();
    }

    @Test
    void recordsRemoteGetResponseSizeWithoutPayloadRetention() throws Exception {
        System.setProperty("citu.memoryForensics.enabled", "true");
        HttpServer server = startServer("{\"ok\":true}");
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/getTransactions";

            String response = UtilUrl.readJsonFromUrl(url, 1_000, 1_000);

            assertEquals("{\"ok\":true}", response);
            Map<String, Object> lastMetrics = lastSampleMetrics();
            assertEquals(response.length(), lastMetrics.get("responseBytes"));
            assertEquals(response.length(), lastMetrics.get("jsonStringLength"));
            assertEquals(url, lastMetrics.get("url"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void capsLargeRemoteResponseAndRecordsFailureStage() throws Exception {
        System.setProperty("citu.memoryForensics.enabled", "true");
        HttpServer server = startServer("[" + "1,".repeat(600) + "1]");
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/winnerList";

            assertThrows(IOException.class, () -> UtilUrl.readJsonFromUrl(url, 1_000, 100));

            Map<String, Object> lastMetrics = lastSampleMetrics();
            assertEquals("IOException", lastMetrics.get("exception"));
            assertEquals(100, lastMetrics.get("maxChars"));
        } finally {
            server.stop(0);
        }
    }

    private static HttpServer startServer(String response) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(bytes);
            }
        });
        server.start();
        return server;
    }

    private static Map<String, Object> lastSampleMetrics() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> traces = (List<Map<String, Object>>) NodeMemoryProfiler.snapshot().get("lastTraces");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> samples = (List<Map<String, Object>>) traces.get(traces.size() - 1).get("stageSamples");
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) samples.get(samples.size() - 1).get("metrics");
        return metrics;
    }
}
