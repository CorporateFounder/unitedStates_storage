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
                ChainTournamentScorer.class
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
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("getTotalPerBlock"));
        assertNotNull(JackpotCalculator.class.getDeclaredMethod("validateSplit"));
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
        assertNotNull(V35TargetDifficulty.class.getDeclaredMethod("applyEmergencyDrop", BigInteger.class, long.class));
        assertNotNull(V35PowValidator.class.getDeclaredMethod("isHashUnderTarget", String.class, BigInteger.class));
        assertNotNull(V35WorkCalculator.class.getDeclaredMethod("zeroWork"));
        assertNotNull(V35WorkCalculator.class.getDeclaredMethod("addWork", BigInteger.class, BigInteger.class));
        assertNotNull(V35WorkCalculator.class.getDeclaredMethod("workFromTarget", BigInteger.class));
        assertNotNull(ChainTournamentScorer.class.getDeclaredMethod("intendedPriority"));
    }
}
