package com.example.website.citu.utils;

import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilUrlTimeoutTest {

    @Test
    void httpConnectionsUseShortEmergencyTimeouts() throws Exception {
        HttpURLConnection connection = UtilUrl.openConnection("http://127.0.0.1:1/size", "GET");

        assertEquals(1500, connection.getConnectTimeout());
        assertEquals(3000, connection.getReadTimeout());
        assertEquals("GET", connection.getRequestMethod());
    }
}
