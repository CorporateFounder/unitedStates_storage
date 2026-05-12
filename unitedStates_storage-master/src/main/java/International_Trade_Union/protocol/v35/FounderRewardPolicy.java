package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * Placeholder for V35 founder reward policy. Legacy founder rewards are not reimplemented here.
 */
public final class FounderRewardPolicy {
    private FounderRewardPolicy() {
    }

    public static boolean isFounderRewardEnabled(long index) {
        return ProtocolVersion.isV35(index) && Seting.V35_FOUNDER_REWARD_ENABLED;
    }

    public static double expectedFounderGoldReward(long index) {
        return isFounderRewardEnabled(index) ? Seting.V35_EXPECTED_FOUNDER_REWARD : 0.00;
    }
}
