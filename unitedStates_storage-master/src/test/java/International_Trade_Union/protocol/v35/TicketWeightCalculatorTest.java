package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicketWeightCalculatorTest {
    private static final double DELTA = 0.00000001;

    @Test
    public void capsTicketBoostsAndKeepsBaseWeight() {
        assertEquals(1.00, TicketWeightCalculator.baseWeight(), DELTA);
        assertEquals(1.50, TicketWeightCalculator.maxWeight(), DELTA);
        assertEquals(0.30, TicketWeightCalculator.capTxBoost(999.00), DELTA);
        assertEquals(0.20, TicketWeightCalculator.capStakeBoost(999.00), DELTA);
        assertEquals(0.00, TicketWeightCalculator.capTxBoost(-1.00), DELTA);
        assertEquals(0.00, TicketWeightCalculator.capStakeBoost(-1.00), DELTA);
        assertEquals(1.00, TicketWeightCalculator.combine(0.00, 0.00), DELTA);
        assertEquals(1.50, TicketWeightCalculator.combine(0.30, 0.20), DELTA);
        assertEquals(1.50, TicketWeightCalculator.combine(999.00, 999.00), DELTA);
        assertEquals(1.00, TicketWeightCalculator.combine(-1.00, -1.00), DELTA);
    }
}
