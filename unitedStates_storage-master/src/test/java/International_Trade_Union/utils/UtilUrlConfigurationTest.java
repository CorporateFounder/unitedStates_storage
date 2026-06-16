package International_Trade_Union.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilUrlConfigurationTest {
    @Test
    void applicationPropertiesKeepPeerResponseCapsConfigured() throws IOException {
        String properties = Files.readString(Path.of("src", "main", "resources", "application.properties"));

        assertTrue(properties.contains("citu.peer.maxResponseBytes.default=1000000"));
        assertTrue(properties.contains("citu.peer.maxWinnerListBytes=200000"));
        assertTrue(properties.contains("citu.peer.maxTransactionsBytes=500000"));
        assertTrue(properties.contains("citu.peer.maxSubBlocksBytes=750000"));
        assertTrue(properties.contains("citu.peer.maxDataShortBytes=100000"));
        assertTrue(properties.contains("citu.peer.connectTimeoutMs=2000"));
        assertTrue(properties.contains("citu.peer.readTimeoutMs=3000"));
        assertTrue(properties.contains("citu.resolve.maxDurationMs=5000"));
        assertTrue(properties.contains("citu.peer.staleBehindBlocks=1000"));
    }
}
