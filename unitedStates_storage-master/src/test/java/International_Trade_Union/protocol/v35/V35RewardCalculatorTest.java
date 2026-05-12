package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35RewardCalculatorTest {
    private static final double DELTA = 0.00000001;

    @Test
    public void exposesFixedRewardConstants() {
        assertEquals(10.00, V35RewardCalculator.getTotalGoldReward(), DELTA);
        assertEquals(8.00, V35RewardCalculator.getInstantMinerGoldReward(), DELTA);
        assertEquals(2.00, V35RewardCalculator.getJackpotGoldReward(), DELTA);
        assertTrue(V35RewardCalculator.validateJackpotSplit());
    }
}
