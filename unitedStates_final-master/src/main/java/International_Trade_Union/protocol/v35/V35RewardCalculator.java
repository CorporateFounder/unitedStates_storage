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

    public static double getJackpotGoldReward() {
        return Seting.V35_JACKPOT_REWARD;
    }

    public static boolean validateJackpotSplit() {
        return Math.abs(JackpotCalculator.getTotalPerBlock() - getJackpotGoldReward()) < 0.00000001;
    }
}
