package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FounderRewardPolicyTest {
    private static final double DELTA = 0.00000001;

    @Test
    public void disablesFounderRewardInV35Skeleton() {
        assertFalse(FounderRewardPolicy.isFounderRewardEnabled(Long.MAX_VALUE));
        assertEquals(0.00, FounderRewardPolicy.expectedFounderGoldReward(Long.MAX_VALUE), DELTA);
        assertFalse(FounderRewardPolicy.isFounderRewardEnabled(0));
        assertEquals(0.00, FounderRewardPolicy.expectedFounderGoldReward(0), DELTA);
    }
}
