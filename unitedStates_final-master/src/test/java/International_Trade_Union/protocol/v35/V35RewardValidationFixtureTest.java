package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35RewardValidationFixtureTest {
    private static final double DELTA = 0.00000001;
    private static final long SIMULATED_V35_INDEX = Long.MAX_VALUE;
    private static final String MINER_ADDRESS = "test-miner-address";
    private static final String FOUNDER_ADDRESS = "test-founder-address";
    private static final String OTHER_ADDRESS = "test-other-address";

    @Test
    public void fixedV35RewardShapeIsLocked() {
        assertEquals(8.00, V35RewardCalculator.getTotalGoldReward(), DELTA);
        assertEquals(6.00, V35RewardCalculator.getInstantMinerGoldReward(), DELTA);
        assertEquals(2.00, V35RewardCalculator.getJackpotGoldReward(), DELTA);
        assertEquals(0.00, V35RewardCalculator.getFounderGoldReward(), DELTA);
        assertEquals(0.00, V35RewardCalculator.getInstantMinerStockReward(), DELTA);
        assertEquals(0.00, V35RewardCalculator.getFounderStockReward(), DELTA);
        assertTrue(V35RewardCalculator.validateJackpotSplit());
    }

    @Test
    public void founderRewardTransactionIsRejectedByV35RewardRules() {
        List<DtoTransaction> transactions = Collections.singletonList(
                rewardTo(FOUNDER_ADDRESS, 0.00, 0.00));

        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, transactions));
    }

    @Test
    public void exactMinerRewardTransactionIsAcceptedByV35RewardRules() {
        List<DtoTransaction> transactions = Collections.singletonList(
                rewardTo(MINER_ADDRESS, 6.00, 0.00));

        assertTrue(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, transactions));
    }

    @Test
    public void minerRewardOverpaymentIsRejectedByV35RewardRules() {
        List<DtoTransaction> transactions = Collections.singletonList(
                rewardTo(MINER_ADDRESS, 6.01, 0.00));

        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, transactions));
    }

    @Test
    public void minerRewardUnderpaymentAndStockRewardAreRejectedByV35RewardRules() {
        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX,
                Collections.singletonList(rewardTo(MINER_ADDRESS, 5.99, 0.00))));
        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX,
                Collections.singletonList(rewardTo(MINER_ADDRESS, 6.00, 0.01))));
    }

    @Test
    public void missingOrExtraBasisRewardShapesAreRejectedByV35RewardRules() {
        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, Collections.emptyList()));
        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, Arrays.asList(
                rewardTo(MINER_ADDRESS, 6.00, 0.00),
                rewardTo(MINER_ADDRESS, 6.00, 0.00))));
        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, Collections.singletonList(
                rewardTo(OTHER_ADDRESS, 6.00, 0.00))));
    }

    @Test
    public void normalIndexesStayOnLegacyPathWhileV35IsDisabled() {
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertFalse(ProtocolVersion.isV35(1_000_000L));
        assertTrue(ProtocolVersion.isLegacy(1_000_000L));

        assertTrue(isAcceptedByV35RewardShape(1_000_000L, Collections.singletonList(
                rewardTo(FOUNDER_ADDRESS, 999.00, 999.00))));
    }

    private static DtoTransaction rewardTo(String customer, double digitalDollar, double digitalStockBalance) {
        return new DtoTransaction(
                Seting.BASIS_ADDRESS,
                customer,
                digitalDollar,
                digitalStockBalance,
                new Laws(),
                0.00,
                VoteEnum.YES);
    }

    private static boolean isAcceptedByV35RewardShape(long index, List<DtoTransaction> transactions) {
        if (!ProtocolVersion.isV35(index)) {
            return true;
        }

        int basisRewardCount = 0;
        for (DtoTransaction transaction : transactions) {
            if (!Seting.BASIS_ADDRESS.equals(transaction.getSender())) {
                continue;
            }
            if (FOUNDER_ADDRESS.equals(transaction.getCustomer())) {
                return false;
            }
            if (!MINER_ADDRESS.equals(transaction.getCustomer())) {
                return false;
            }
            basisRewardCount += 1;
            if (!V35RewardCalculator.isValidInstantMinerReward(
                    transaction.getDigitalDollar(),
                    transaction.getDigitalStockBalance())) {
                return false;
            }
            if (basisRewardCount > 1) {
                return false;
            }
        }
        return basisRewardCount == 1;
    }
}
