package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        assertEquals(2.00, JackpotCalculator.totalReservePerBlock(), DELTA);
        assertTrue(JackpotCalculator.validateSplit());
        assertTrue(JackpotCalculator.validateReserveSplit());
    }

    @Test
    public void returnsReservePerBlockByPeriod() {
        assertEquals(0.70, JackpotCalculator.reservePerBlockForPeriod(JackpotCalculator.PERIOD_DAILY), DELTA);
        assertEquals(0.40, JackpotCalculator.reservePerBlockForPeriod(JackpotCalculator.PERIOD_WEEKLY), DELTA);
        assertEquals(0.30, JackpotCalculator.reservePerBlockForPeriod(JackpotCalculator.PERIOD_MONTHLY), DELTA);
        assertEquals(0.25, JackpotCalculator.reservePerBlockForPeriod(JackpotCalculator.PERIOD_QUARTERLY), DELTA);
        assertEquals(0.20, JackpotCalculator.reservePerBlockForPeriod(JackpotCalculator.PERIOD_HALF_YEAR), DELTA);
        assertEquals(0.15, JackpotCalculator.reservePerBlockForPeriod(JackpotCalculator.PERIOD_YEARLY), DELTA);
        assertEquals(0.0, JackpotCalculator.reservePerBlockForPeriod("unknown"), DELTA);
        assertEquals(0.0, JackpotCalculator.reservePerBlockForPeriod(null), DELTA);
    }

    @Test
    public void exposesFixedBlockWindows() {
        assertEquals(576L, JackpotCalculator.dailyWindowBlocks());
        assertEquals(4032L, JackpotCalculator.weeklyWindowBlocks());
        assertEquals(17280L, JackpotCalculator.monthlyWindowBlocks());
        assertEquals(51840L, JackpotCalculator.quarterlyWindowBlocks());
        assertEquals(104832L, JackpotCalculator.halfYearWindowBlocks());
        assertEquals(210240L, JackpotCalculator.yearlyWindowBlocks());
    }

    @Test
    public void periodBoundariesAreDeterministic() {
        long daily = JackpotCalculator.dailyWindowBlocks();

        assertEquals(0L, JackpotCalculator.periodStart(0L, daily));
        assertEquals(0L, JackpotCalculator.periodStart(575L, daily));
        assertEquals(576L, JackpotCalculator.periodStart(576L, daily));
        assertEquals(575L, JackpotCalculator.periodEnd(0L, daily));
        assertEquals(575L, JackpotCalculator.periodEnd(575L, daily));
        assertEquals(1151L, JackpotCalculator.periodEnd(576L, daily));
        assertFalse(JackpotCalculator.isPeriodEnd(574L, daily));
        assertTrue(JackpotCalculator.isPeriodEnd(575L, daily));
        assertEquals(635L, JackpotCalculator.maturityBlock(575L, JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS));
    }

    @Test
    public void invalidPeriodInputsAreSafe() {
        assertEquals(-1L, JackpotCalculator.periodStart(-1L, 576L));
        assertEquals(-1L, JackpotCalculator.periodStart(1L, 0L));
        assertEquals(-1L, JackpotCalculator.periodEnd(-1L, 576L));
        assertEquals(-1L, JackpotCalculator.maturityBlock(-1L, 60L));
        assertEquals(-1L, JackpotCalculator.maturityBlock(10L, -1L));
        assertFalse(JackpotCalculator.isPeriodEnd(10L, 0L));
    }

    @Test
    public void winnerOffsetIsDeterministicAndBounded() {
        String seed = "000000000000000000000000000000000000000000000000000000000000000f";

        assertEquals(5, JackpotCalculator.selectWinnerOffset(seed, 10));
        assertEquals(JackpotCalculator.selectWinnerOffset(seed, 10), JackpotCalculator.selectWinnerOffset(seed, 10));
        assertEquals(0, JackpotCalculator.selectWinnerOffset("0x10", 4));
        assertTrue(JackpotCalculator.selectWinnerOffset(seed, 10) >= 0);
        assertTrue(JackpotCalculator.selectWinnerOffset(seed, 10) < 10);
    }

    @Test
    public void winnerOffsetRejectsInvalidInputs() {
        assertEquals(-1, JackpotCalculator.selectWinnerOffset("01", 0));
        assertEquals(-1, JackpotCalculator.selectWinnerOffset("not-hex", 10));
        assertEquals(-1, JackpotCalculator.selectWinnerOffset(null, 10));
        assertEquals(-1, JackpotCalculator.selectWinnerOffset("", 10));
    }

    @Test
    public void seedCombinationIsDeterministicAndOrderSensitive() {
        String first = JackpotCalculator.combineHashesForSeed(Arrays.asList("aa", "bb", "cc"));
        String second = JackpotCalculator.combineHashesForSeed(Arrays.asList("aa", "bb", "cc"));
        String reordered = JackpotCalculator.combineHashesForSeed(Arrays.asList("cc", "bb", "aa"));

        assertEquals(first, second);
        assertNotEquals(first, reordered);
        assertEquals(64, first.length());
    }

    @Test
    public void seedCombinationHandlesInvalidInputsSafely() {
        assertEquals("", JackpotCalculator.combineHashesForSeed(null));
        assertEquals("", JackpotCalculator.combineHashesForSeed(Collections.emptyList()));
        assertEquals("", JackpotCalculator.combineHashesForSeed(Arrays.asList(null, "", "   ")));
        assertEquals(JackpotCalculator.combineHashesForSeed(Collections.singletonList("aa")),
                JackpotCalculator.combineHashesForSeed(Arrays.asList(null, "aa", "")));
    }

    @Test
    public void baseTicketEligibilityUsesOnlyV35BlockAndMinerAddress() {
        assertEquals(1, JackpotCalculator.baseTicketCountForBlock());
        assertTrue(JackpotCalculator.isEligibleTicketBlock(true, "miner-address"));
        assertFalse(JackpotCalculator.isEligibleTicketBlock(false, "miner-address"));
        assertFalse(JackpotCalculator.isEligibleTicketBlock(true, ""));
        assertFalse(JackpotCalculator.isEligibleTicketBlock(true, "   "));
        assertFalse(JackpotCalculator.isEligibleTicketBlock(true, null));
    }
}
