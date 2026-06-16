package International_Trade_Union.logger;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerLoggingConfigurationTest {

    @Test
    void logbackConfigCapsNodeAndErrorLogs() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("logback-spring.xml");
        assertNotNull(inputStream);
        String config = new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertTrue(config.contains("source=\"logging.file.path\""));
        assertTrue(config.contains("source=\"logging.level.root\""));
        assertTrue(config.contains("C:/server/logs"));
        assertTrue(config.contains("citu-node.log"));
        assertTrue(config.contains("citu-node.%d{yyyy-MM-dd}.%i.log.gz"));
        assertTrue(config.contains("<maxFileSize>2MB</maxFileSize>"));
        assertTrue(config.contains("<maxHistory>3</maxHistory>"));
        assertTrue(config.contains("<totalSizeCap>6MB</totalSizeCap>"));
        assertTrue(config.contains("citu-node-error.log"));
        assertTrue(config.contains("<maxFileSize>1MB</maxFileSize>"));
        assertTrue(config.contains("<maxHistory>2</maxHistory>"));
        assertTrue(config.contains("<totalSizeCap>2MB</totalSizeCap>"));
    }

    @Test
    void sanitizerTruncatesHugeMessagesAndRemovesLineBreaks() {
        String hugePayload = "winnerList=" + "x".repeat(1000) + "\nnext-line";

        String safe = ServerLogSanitizer.safeMessage(hugePayload);

        assertTrue(safe.length() < 380);
        assertTrue(safe.contains("[truncated"));
        assertTrue(!safe.contains("\n"));
    }

    @Test
    void constantsRemainActivationPreparedAndBurnPlaceholder() {
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }
}
