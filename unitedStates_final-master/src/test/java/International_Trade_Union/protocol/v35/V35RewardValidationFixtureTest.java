package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    public void jackpotPayoutCandidateIsClassifiedButRejectedUntilProofContextExists() {
        DtoTransaction dailyJackpot = rewardTo(OTHER_ADDRESS,
                JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.DAILY).doubleValue(), 0.00);

        assertEquals(JackpotCalculator.JackpotPeriod.DAILY,
                JackpotCalculator.classifyPayoutAmount(dailyJackpot.getDigitalDollar()));
        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, Arrays.asList(
                rewardTo(MINER_ADDRESS, 6.00, 0.00),
                dailyJackpot)));
    }

    @Test
    public void jackpotPayoutCandidateWithStockOrBonusIsRejected() {
        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, Arrays.asList(
                rewardTo(MINER_ADDRESS, 6.00, 0.00),
                rewardTo(OTHER_ADDRESS, 302.40, 0.01))));
        assertFalse(isAcceptedByV35RewardShape(SIMULATED_V35_INDEX, Arrays.asList(
                rewardTo(MINER_ADDRESS, 6.00, 0.00),
                rewardTo(OTHER_ADDRESS, 302.40, 0.00, 0.01))));
    }

    @Test
    public void duplicateJackpotPeriodCandidatesAreDetectableBeforeFullProofValidation() {
        assertTrue(hasDuplicateJackpotPeriodCandidates(Arrays.asList(
                rewardTo("winner-a", 302.40, 0.00),
                rewardTo("winner-b", 302.40, 0.00))));
        assertFalse(hasDuplicateJackpotPeriodCandidates(Arrays.asList(
                rewardTo("winner-a", 302.40, 0.00),
                rewardTo("winner-b", 1209.60, 0.00))));
    }

    @Test
    public void normalIndexesStayOnLegacyPathWhileV35IsDisabled() {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertFalse(ProtocolVersion.isV35(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex()));
        assertTrue(ProtocolVersion.isLegacy(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex()));

        assertTrue(isAcceptedByV35RewardShape(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex(), Collections.singletonList(
                rewardTo(FOUNDER_ADDRESS, 999.00, 999.00))));
    }

    private static DtoTransaction rewardTo(String customer, double digitalDollar, double digitalStockBalance) {
        return rewardTo(customer, digitalDollar, digitalStockBalance, 0.00);
    }

    private static DtoTransaction rewardTo(String customer, double digitalDollar, double digitalStockBalance, double bonusForMiner) {
        return new DtoTransaction(
                Seting.BASIS_ADDRESS,
                customer,
                digitalDollar,
                digitalStockBalance,
                new Laws(),
                bonusForMiner,
                VoteEnum.YES);
    }

    private static boolean isAcceptedByV35RewardShape(long index, List<DtoTransaction> transactions) {
        if (!ProtocolVersion.isV35(index)) {
            return true;
        }

        int basisRewardCount = 0;
        Set<JackpotCalculator.JackpotPeriod> jackpotPeriods = EnumSet.noneOf(JackpotCalculator.JackpotPeriod.class);
        for (DtoTransaction transaction : transactions) {
            if (!Seting.BASIS_ADDRESS.equals(transaction.getSender())) {
                continue;
            }
            if (FOUNDER_ADDRESS.equals(transaction.getCustomer())) {
                return false;
            }
            if (MINER_ADDRESS.equals(transaction.getCustomer())
                    && V35RewardCalculator.isValidInstantMinerReward(transaction.getDigitalDollar(), transaction.getDigitalStockBalance())) {
                basisRewardCount += 1;
                if (basisRewardCount > 1) {
                    return false;
                }
            } else {
                JackpotCalculator.JackpotPeriod jackpotPeriod = JackpotCalculator.classifyPayoutAmount(transaction.getDigitalDollar());
                if (jackpotPeriod == JackpotCalculator.JackpotPeriod.UNKNOWN
                        || transaction.getDigitalStockBalance() != V35RewardCalculator.getInstantMinerStockReward()
                        || transaction.getBonusForMiner() != 0.0
                        || !jackpotPeriods.add(jackpotPeriod)) {
                    return false;
                }
                return false;
            }
        }
        return basisRewardCount == 1;
    }

    private static boolean hasDuplicateJackpotPeriodCandidates(List<DtoTransaction> transactions) {
        Set<JackpotCalculator.JackpotPeriod> jackpotPeriods = EnumSet.noneOf(JackpotCalculator.JackpotPeriod.class);
        for (DtoTransaction transaction : transactions) {
            JackpotCalculator.JackpotPeriod jackpotPeriod = JackpotCalculator.classifyPayoutAmount(transaction.getDigitalDollar());
            if (jackpotPeriod == JackpotCalculator.JackpotPeriod.UNKNOWN) {
                continue;
            }
            if (!jackpotPeriods.add(jackpotPeriod)) {
                return true;
            }
        }
        return false;
    }
}
