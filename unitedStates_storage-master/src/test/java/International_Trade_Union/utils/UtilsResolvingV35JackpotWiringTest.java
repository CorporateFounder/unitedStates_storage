package International_Trade_Union.utils;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.protocol.v35.JackpotCalculator;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsResolvingV35JackpotWiringTest {

    @Test
    public void jackpotCandidateIsDetectedButAccountingGateStaysDisabled() {
        Block block = block(10L, transactions(
                basisTransaction("miner", 6.00),
                basisTransaction("winner", JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.DAILY).doubleValue())
        ));

        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertTrue(UtilsResolving.containsV35JackpotPayoutCandidate(block));
        assertFalse(UtilsResolving.isV35JackpotAccountingRequired(block));
        assertFalse(UtilsResolving.requiresV35JackpotAccounting(Arrays.asList(block)));
    }

    @Test
    public void jackpotCandidateStripperKeepsMinerRewardAndRemovesJackpotPayouts() {
        Block block = block(10L, transactions(
                basisTransaction("miner", 6.00),
                basisTransaction("daily-winner", JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.DAILY).doubleValue()),
                basisTransaction("weekly-winner", JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.WEEKLY).doubleValue())
        ));

        Block stripped = UtilsResolving.withoutV35JackpotPayoutCandidates(block);

        assertEquals(1, stripped.getDtoTransactions().size());
        assertEquals(6.00, stripped.getDtoTransactions().get(0).getDigitalDollar());
        assertEquals("miner", stripped.getDtoTransactions().get(0).getCustomer());
    }

    @Test
    public void listCandidateDetectionIsSafeForNullAndEmptyValues() {
        assertFalse(UtilsResolving.containsV35JackpotPayoutCandidate((List<Block>) null));
        assertFalse(UtilsResolving.containsV35JackpotPayoutCandidate(block(1L, transactions())));
        assertTrue(UtilsResolving.containsV35JackpotPayoutCandidate(Arrays.asList(
                block(1L, transactions()),
                block(2L, transactions(basisTransaction("winner", 302.40)))
        )));
    }

    private static Block block(long index, List<DtoTransaction> transactions) {
        return new Block(transactions, "prev", "miner", "founder", 0L, 0.0, 0L,
                new Timestamp(index * 1_000L), index, "hash-" + index);
    }

    private static List<DtoTransaction> transactions(DtoTransaction... transactions) {
        return Arrays.asList(transactions);
    }

    private static DtoTransaction basisTransaction(String customer, double amount) {
        return new DtoTransaction(Seting.BASIS_ADDRESS, customer, amount, 0.0, new Laws(), 0.0, VoteEnum.YES);
    }
}
