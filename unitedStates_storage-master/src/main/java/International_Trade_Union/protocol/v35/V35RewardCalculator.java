package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * Read-only V35 reward constants. This class does not create transactions or mutate blocks.
 */
public final class V35RewardCalculator {
    private V35RewardCalculator() {
    }

    public static double getTotalGoldReward() {
        return Seting.V35_GOLD_REWARD;
    }

    public static double getInstantMinerGoldReward() {
        return Seting.V35_INSTANT_MINER_REWARD;
    }

    public static double getInstantMinerStockReward() {
        return 0.00;
    }

    public static double getJackpotGoldReward() {
        return Seting.V35_JACKPOT_REWARD;
    }

    public static double getFounderGoldReward() {
        return Seting.V35_EXPECTED_FOUNDER_REWARD;
    }

    public static double getFounderStockReward() {
        return 0.00;
    }

    public static boolean validateJackpotSplit() {
        return Math.abs(JackpotCalculator.getTotalPerBlock() - getJackpotGoldReward()) < 0.00000001;
    }

    public static boolean isValidInstantMinerReward(double digitalDollar, double digitalStockBalance) {
        return isEqual(digitalDollar, getInstantMinerGoldReward())
                && isEqual(digitalStockBalance, getInstantMinerStockReward());
    }

    public static boolean isValidFounderReward(double digitalDollar, double digitalStockBalance) {
        return isEqual(digitalDollar, getFounderGoldReward())
                && isEqual(digitalStockBalance, getFounderStockReward());
    }

    private static boolean isEqual(double first, double second) {
        return Math.abs(first - second) < 0.00000001;
    }
}
