package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35RewardCalculatorTest {
    private static final double DELTA = 0.00000001;

    @Test
    public void exposesFixedRewardConstants() {
        assertEquals(8.00, V35RewardCalculator.getTotalGoldReward(), DELTA);
        assertEquals(6.00, V35RewardCalculator.getInstantMinerGoldReward(), DELTA);
        assertEquals(0.00, V35RewardCalculator.getInstantMinerStockReward(), DELTA);
        assertEquals(2.00, V35RewardCalculator.getJackpotGoldReward(), DELTA);
        assertEquals(0.00, V35RewardCalculator.getFounderGoldReward(), DELTA);
        assertEquals(0.00, V35RewardCalculator.getFounderStockReward(), DELTA);
        assertTrue(V35RewardCalculator.validateJackpotSplit());
    }

    @Test
    public void validatesV35MinerAndFounderRewardShape() {
        assertTrue(V35RewardCalculator.isValidInstantMinerReward(6.00, 0.00));
        assertFalse(V35RewardCalculator.isValidInstantMinerReward(6.01, 0.00));
        assertFalse(V35RewardCalculator.isValidInstantMinerReward(6.00, 1.00));
        assertTrue(V35RewardCalculator.isValidFounderReward(0.00, 0.00));
        assertFalse(V35RewardCalculator.isValidFounderReward(6.00, 0.00));
    }
}
