package International_Trade_Union.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilUrlMemoryGuardTest {

    @Test
    void openConnectionAppliesConfiguredTimeouts() throws Exception {
        URLConnection connection = UtilUrl.openConnection("http://127.0.0.1:1/size", 1234, 2345);

        assertEquals(1234, connection.getConnectTimeout());
        assertEquals(2345, connection.getReadTimeout());
    }

    @Test
    void readAllRejectsOversizedGetResponses() {
        IOException exception = assertThrows(
                IOException.class,
                () -> UtilUrl.readAll(new StringReader("abcdef"), 3)
        );

        assertTrue(exception.getMessage().contains(UtilUrl.TOO_LARGE_REASON));
    }

    @Test
    void defaultPeerHttpLimitsStayBounded() {
        assertTrue(UtilUrl.DEFAULT_CONNECT_TIMEOUT_MILLIS <= 3000);
        assertTrue(UtilUrl.DEFAULT_READ_TIMEOUT_MILLIS <= 6000);
        assertTrue(UtilUrl.SILENT_TIMEOUT_MILLIS <= 3000);
        assertTrue(UtilUrl.MAX_GET_RESPONSE_CHARS <= 1_000_000);
        assertTrue(UtilUrl.MAX_WINNER_LIST_RESPONSE_CHARS <= 200_000);
        assertTrue(UtilUrl.MAX_GET_NODES_RESPONSE_CHARS <= 100_000);
        assertTrue(UtilUrl.MAX_GET_TRANSACTIONS_RESPONSE_CHARS <= 500_000);
        assertTrue(UtilUrl.MAX_SUB_BLOCKS_RESPONSE_CHARS <= 750_000);
        assertTrue(UtilUrl.MAX_DATASHORT_RESPONSE_CHARS <= 100_000);
    }

    @Test
    void endpointSpecificResponseCapsAreSelectedBeforeParsing() {
        assertEquals(UtilUrl.MAX_WINNER_LIST_RESPONSE_CHARS, UtilUrl.maxResponseCharsForUrl("http://peer/winnerList"));
        assertEquals(UtilUrl.MAX_GET_TRANSACTIONS_RESPONSE_CHARS, UtilUrl.maxResponseCharsForUrl("http://peer/getTransactions"));
        assertEquals(UtilUrl.MAX_SUB_BLOCKS_RESPONSE_CHARS, UtilUrl.maxResponseCharsForUrl("http://peer/sub-blocks"));
        assertEquals(UtilUrl.MAX_DATASHORT_RESPONSE_CHARS, UtilUrl.maxResponseCharsForUrl("http://peer/datashort"));
        assertEquals(UtilUrl.MAX_GET_RESPONSE_CHARS, UtilUrl.maxResponseCharsForUrl("http://peer/size"));
    }
}
