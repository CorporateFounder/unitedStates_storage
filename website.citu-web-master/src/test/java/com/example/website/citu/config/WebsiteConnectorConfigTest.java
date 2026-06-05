package com.example.website.citu.config;

import org.apache.catalina.connector.Connector;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebsiteConnectorConfigTest {

    @Test
    void httpOnlyModeDoesNotCreateAdditionalConnector() {
        WebsiteConnectorConfig config = new WebsiteConnectorConfig(false, true, 80, 80);

        assertFalse(config.shouldAddAdditionalHttpConnector());
    }

    @Test
    void httpsModeCanCreateAdditionalHttpConnector() {
        WebsiteConnectorConfig config = new WebsiteConnectorConfig(true, true, 443, 80);

        assertTrue(config.shouldAddAdditionalHttpConnector());
        Connector connector = config.createAdditionalHttpConnector();
        assertEquals(80, connector.getPort());
        assertEquals("http", connector.getScheme());
        assertFalse(connector.getSecure());
    }

    @Test
    void additionalConnectorIsSkippedWhenPortsMatch() {
        WebsiteConnectorConfig config = new WebsiteConnectorConfig(true, true, 443, 443);

        assertFalse(config.shouldAddAdditionalHttpConnector());
    }

    @Test
    void productionDefaultsStayEmergencyHttpOnly() throws IOException {
        String properties = Files.readString(Path.of("src", "main", "resources", "application.properties"));

        assertTrue(properties.contains("server.port=80"));
        assertTrue(properties.contains("server.ssl.enabled=false"));
        assertTrue(properties.contains("citu.http.additional.enabled=false"));
        assertTrue(properties.contains("citu.https.redirect.enabled=false"));
        assertTrue(properties.contains("citu.number-formatting.enabled=false"));
    }
}
