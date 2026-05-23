package International_Trade_Union.controllers;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35DashboardApiClientTest {
    @Test
    public void fetchUsesOnlyReadOnlyV35Endpoints() {
        List<String> urls = new ArrayList<>();
        V35DashboardApiClient client = new V35DashboardApiClient((url, timeoutMillis) -> {
            urls.add(url);
            return "{}";
        }, () -> "http://node.test/");

        V35DashboardApiClient.LiveData data = client.fetch();

        assertEquals("http://node.test", data.baseUrl());
        assertEquals(5, urls.size());
        assertTrue(urls.contains("http://node.test/v35/protocol"));
        assertTrue(urls.contains("http://node.test/v35/supply"));
        assertTrue(urls.contains("http://node.test/v35/burn"));
        assertTrue(urls.contains("http://node.test/v35/founder"));
        assertTrue(urls.contains("http://node.test/v35/founder/burn-snapshot"));
        assertTrue(urls.stream().allMatch(url -> url.contains("/v35/")));
        assertTrue(urls.stream().noneMatch(url -> url.contains("/mine") || url.contains("/send")));
    }

    @Test
    public void malformedJsonDoesNotCrashDashboardClient() {
        V35DashboardApiClient client = new V35DashboardApiClient((url, timeoutMillis) -> "not-json",
                () -> "http://node.test");

        V35DashboardApiClient.LiveData data = client.fetch();

        assertFalse(data.protocolAvailable());
        assertFalse(data.supplyAvailable());
        assertTrue(data.errorMessage().contains("Live V35 data unavailable"));
    }

    @Test
    public void normalizesServerUrl() {
        assertEquals("http://node.test", V35DashboardApiClient.normalizeBaseUrl(" http://node.test/// "));
        assertEquals("", V35DashboardApiClient.normalizeBaseUrl("  "));
    }
}
