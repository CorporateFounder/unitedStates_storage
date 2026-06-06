package International_Trade_Union.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AllTransactionsMiningFetchTest {

    @Test
    void getTransactionsTimeoutIsShortEnoughForMiningStartup() {
        assertTrue(AllTransactions.GET_TRANSACTIONS_TIMEOUT_MILLIS <= 2500);
    }

    @Test
    void unavailableRemoteTransactionsFallbackToEmptyList() {
        assertTrue(AllTransactions.fetchRemoteTransactions("http://127.0.0.1:1").isEmpty());
    }
}
