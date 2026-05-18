package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class V35ParityTest {
    private static final String PACKAGE_NAME = "International_Trade_Union.protocol.v35";

    @Test
    public void skeletonClassesUseExpectedPackage() {
        Class<?>[] classes = {
                ProtocolVersion.class,
                V35RewardCalculator.class,
                FounderRewardPolicy.class,
                JackpotCalculator.class,
                TicketWeightCalculator.class,
                AssetDisplayMapper.class,
                BurnAddressPolicy.class,
                V35TargetDifficulty.class,
                V35PowValidator.class,
                V35WorkCalculator.class,
                ChainTournamentScorer.class,
                JackpotTicket.class,
                JackpotWinnerProof.class
        };

        for (Class<?> clazz : classes) {
            assertEquals(PACKAGE_NAME, clazz.getPackage().getName());
        }
    }

    @Test
    public void skeletonClassesExposeExpectedMethods() throws Exception {
        assertNotNull(ProtocolVersion.class.getDeclaredMethod("isV35", long.class));
        assertNotNull(ProtocolVersion.class.getDeclaredMethod("isLegacy", long.class));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("getTotalGoldReward"));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("getInstantMinerGoldReward"));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("getInstantMinerStockReward"));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("getJackpotGoldReward"));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("getFounderGoldReward"));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("getFounderStockReward"));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("validateJackpotSplit"));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("isValidInstantMinerReward", double.class, double.class));
        assertNotNull(V35RewardCalculator.class.getDeclaredMethod("isValidFounderReward", double.class, double.class));
        assertNotNull(FounderRewardPolicy.class.getDeclaredMethod("isFounderRewardEnabled", long.class));
        assertNotNull(FounderRewardPolicy.class.getDeclaredMethod("expectedFounderGoldReward", long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("getDailyPerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("getWeeklyPerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("getMonthlyPerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("getQuarterlyPerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("getHalfYearPerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("getYearlyPerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("reservePerBlockForPeriod", String.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("isKnownJackpotPeriod", String.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("windowBlocksForPeriod", String.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("getTotalPerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("totalReservePerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("validateSplit"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("validateReserveSplit"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("expectedPayoutAmount", String.class, int.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("dailyWindowBlocks"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("weeklyWindowBlocks"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("monthlyWindowBlocks"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("quarterlyWindowBlocks"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("halfYearWindowBlocks"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("yearlyWindowBlocks"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("periodStart", long.class, long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("periodEnd", long.class, long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("isPeriodEnd", long.class, long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("maturityBlock", long.class, long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("periodStartForBlock", long.class, long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("periodEndForBlock", long.class, long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("maturityBlockForPeriod", long.class, long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("selectWinnerOffset", String.class, int.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("combineHashesForSeed", java.util.List.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("isMature", long.class, long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("seedFromFutureHashes", java.util.List.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("baseTicketCountForBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("isEligibleTicketBlock", boolean.class, String.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("isEligibleTicket", long.class, String.class, boolean.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("eligibleTickets", java.util.List.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("selectWinner", long.class, long.class, java.util.List.class, java.util.List.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("selectWinner", java.util.List.class, java.util.List.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("isV35JackpotPayoutAllowedAt", long.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("isValidJackpotPayout",
                long.class, String.class, double.class, String.class, String.class,
                JackpotWinnerProof.class, int.class, boolean.class));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("isValidJackpotPayout",
                boolean.class, long.class, String.class, double.class, String.class, String.class,
                JackpotWinnerProof.class, int.class, boolean.class));
        assertNotNull(JackpotTicket.class.getDeclaredMethod("of", long.class, String.class));
        assertNotNull(JackpotTicket.class.getDeclaredMethod("getBlockIndex"));
        assertNotNull(JackpotTicket.class.getDeclaredMethod("getMinerAddress"));
        assertNotNull(JackpotTicket.class.getDeclaredMethod("getWeight"));
        assertNotNull(JackpotTicket.class.getDeclaredMethod("isValid"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("noWinner", long.class, long.class, long.class, int.class, String.class));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("getPeriodStart"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("getPeriodEnd"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("getMaturityBlock"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("getRandomDelayBlocks"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("getSeedHex"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("getSelectedOffset"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("getSelectedBlockIndex"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("getWinnerMinerAddress"));
        assertNotNull(JackpotWinnerProof.class.getDeclaredMethod("hasWinner"));
        assertNotNull(TicketWeightCalculator.class.getDeclaredMethod("baseWeight"));
        assertNotNull(TicketWeightCalculator.class.getDeclaredMethod("maxWeight"));
        assertNotNull(TicketWeightCalculator.class.getDeclaredMethod("capTxBoost", double.class));
        assertNotNull(TicketWeightCalculator.class.getDeclaredMethod("capStakeBoost", double.class));
        assertNotNull(TicketWeightCalculator.class.getDeclaredMethod("combine", double.class, double.class));
        assertNotNull(AssetDisplayMapper.class.getDeclaredMethod("rawDigitalDollarToGoldDisplay", double.class));
        assertNotNull(AssetDisplayMapper.class.getDeclaredMethod("rawDigitalStockToSilverDisplay", double.class));
        assertNotNull(AssetDisplayMapper.class.getDeclaredMethod("silverDisplayToRawDigitalStock", double.class));
        assertNotNull(BurnAddressPolicy.class.getDeclaredMethod("getBurnAddress"));
        assertNotNull(BurnAddressPolicy.class.getDeclaredMethod("isBurnAddress", String.class));
        assertNotNull(BurnAddressPolicy.class.getDeclaredMethod("canSpendFromAddress", long.class, String.class));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("getTargetWindow"));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("getTargetBlockTimeSeconds"));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("getEmergencyDelaySeconds"));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("getInitialCompactTarget"));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("getInitialTarget"));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("decodeCompactTarget", long.class));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("encodeCompactTarget", BigInteger.class));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("clampTargetChange", BigInteger.class, BigInteger.class));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("calculateNextTarget", BigInteger.class, long.class, long.class));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("calculateNextCompactTarget", long.class, long.class, long.class, long.class));
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("applyEmergencyDrop", BigInteger.class, long.class));
        assertNotNull(V35PowValidator.class.getDeclaredMethod("isHashUnderTarget", String.class, BigInteger.class));
        assertNotNull(V35WorkCalculator.class.getDeclaredMethod("zeroWork"));
        assertNotNull(V35WorkCalculator.class.getDeclaredMethod("addWork", BigInteger.class, BigInteger.class));
        assertNotNull(V35WorkCalculator.class.getDeclaredMethod("workFromTarget", BigInteger.class));
        assertNotNull(V35WorkCalculator.class.getDeclaredMethod("workFromCompactTarget", long.class));
        assertNotNull(V35WorkCalculator.class.getDeclaredMethod("cumulativeWorkFromCompactTargets", java.util.List.class));
        assertNotNull(ChainTournamentScorer.class.getDeclaredMethod("intendedPriority"));
        assertNotNull(ChainTournamentScorer.class.getDeclaredMethod("capTieBreakerScore", double.class));
        assertNotNull(ChainTournamentScorer.class.getDeclaredMethod("score", boolean.class, BigInteger.class, long.class, double.class, String.class));
        assertNotNull(ChainTournamentScorer.class.getDeclaredMethod("compare", ChainTournamentScorer.ChainScore.class, ChainTournamentScorer.ChainScore.class));
        assertNotNull(ChainTournamentScorer.class.getDeclaredMethod("betterOf", ChainTournamentScorer.ChainScore.class, ChainTournamentScorer.ChainScore.class));
    }
}
