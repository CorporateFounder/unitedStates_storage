package International_Trade_Union.controllers;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionControllerMemoryGuardTest {

    @Test
    void nullTransactionListReturnsEmptyResponse() {
        assertTrue(TransactionController.limitTransactionsForResponse(null).isEmpty());
    }

    @Test
    void smallTransactionListIsReturnedWithoutCopy() {
        List<DtoTransaction> transactions = new ArrayList<>(Collections.nCopies(10, null));

        assertSame(transactions, TransactionController.limitTransactionsForResponse(transactions));
    }

    @Test
    void oversizedTransactionListIsCappedForPublicResponse() {
        List<DtoTransaction> transactions = new ArrayList<>(
                Collections.nCopies(TransactionController.MAX_GET_TRANSACTIONS_RESPONSE + 25, null)
        );

        List<DtoTransaction> response = TransactionController.limitTransactionsForResponse(transactions);

        assertEquals(TransactionController.MAX_GET_TRANSACTIONS_RESPONSE, response.size());
    }
}
