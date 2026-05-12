package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JackpotCalculatorTest {
    private static final double DELTA = 0.00000001;

    @Test
    public void exposesJackpotSplitConstants() {
        assertEquals(0.70, JackpotCalculator.getDailyPerBlock(), DELTA);
        assertEquals(0.40, JackpotCalculator.getWeeklyPerBlock(), DELTA);
        assertEquals(0.30, JackpotCalculator.getMonthlyPerBlock(), DELTA);
        assertEquals(0.25, JackpotCalculator.getQuarterlyPerBlock(), DELTA);
        assertEquals(0.20, JackpotCalculator.getHalfYearPerBlock(), DELTA);
        assertEquals(0.15, JackpotCalculator.getYearlyPerBlock(), DELTA);
        assertEquals(2.00, JackpotCalculator.getTotalPerBlock(), DELTA);
        assertTrue(JackpotCalculator.validateSplit());
    }
}
