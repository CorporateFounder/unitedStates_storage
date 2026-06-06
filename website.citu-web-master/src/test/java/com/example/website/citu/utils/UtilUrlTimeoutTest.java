package com.example.website.citu.utils;

import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilUrlTimeoutTest {

    @Test
    void httpConnectionsUseShortEmergencyTimeouts() throws Exception {
        HttpURLConnection connection = UtilUrl.openConnection("http://127.0.0.1:1/size", "GET");

        assertEquals(1500, connection.getConnectTimeout());
        assertEquals(3000, connection.getReadTimeout());
        assertEquals("GET", connection.getRequestMethod());
    }

    @Test
    void failedGetReturnsNullAndDoesNotThrow() {
        assertDoesNotThrow(() -> assertNull(UtilUrl.getObject("http://127.0.0.1:1/size")));
        assertDoesNotThrow(() -> assertNull(UtilUrl.readJsonFromUrl("http://127.0.0.1:1/size")));
    }

    @Test
    void failedPostReturnsNullAndDoesNotThrow() {
        assertDoesNotThrow(() -> assertNull(UtilUrl.getObject("{}", "http://127.0.0.1:1/sub-blocks")));
    }
}
