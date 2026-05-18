package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

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
        assertEquals(432L, JackpotCalculator.dailyWindowBlocks());
        assertEquals(3024L, JackpotCalculator.weeklyWindowBlocks());
        assertEquals(12960L, JackpotCalculator.monthlyWindowBlocks());
        assertEquals(38880L, JackpotCalculator.quarterlyWindowBlocks());
        assertEquals(78624L, JackpotCalculator.halfYearWindowBlocks());
        assertEquals(157680L, JackpotCalculator.yearlyWindowBlocks());
    }

    @Test
    public void exposesFullWindowJackpotPayoutAmounts() {
        assertEquals(302.40, JackpotCalculator.expectedPayoutAmount(
                JackpotCalculator.PERIOD_DAILY, (int) JackpotCalculator.dailyWindowBlocks()), DELTA);
        assertEquals(1209.60, JackpotCalculator.expectedPayoutAmount(
                JackpotCalculator.PERIOD_WEEKLY, (int) JackpotCalculator.weeklyWindowBlocks()), DELTA);
        assertEquals(3888.00, JackpotCalculator.expectedPayoutAmount(
                JackpotCalculator.PERIOD_MONTHLY, (int) JackpotCalculator.monthlyWindowBlocks()), DELTA);
        assertEquals(9720.00, JackpotCalculator.expectedPayoutAmount(
                JackpotCalculator.PERIOD_QUARTERLY, (int) JackpotCalculator.quarterlyWindowBlocks()), DELTA);
        assertEquals(15724.80, JackpotCalculator.expectedPayoutAmount(
                JackpotCalculator.PERIOD_HALF_YEAR, (int) JackpotCalculator.halfYearWindowBlocks()), DELTA);
        assertEquals(23652.00, JackpotCalculator.expectedPayoutAmount(
                JackpotCalculator.PERIOD_YEARLY, (int) JackpotCalculator.yearlyWindowBlocks()), DELTA);
    }

    @Test
    public void classifiesCanonicalJackpotPayoutAmounts() {
        assertEquals(JackpotCalculator.JackpotPeriod.DAILY, JackpotCalculator.classifyPayoutAmount(302.40));
        assertEquals(JackpotCalculator.JackpotPeriod.DAILY, JackpotCalculator.classifyPayoutAmount(302.400));
        assertEquals(JackpotCalculator.JackpotPeriod.WEEKLY, JackpotCalculator.classifyPayoutAmount(1209.60));
        assertEquals(JackpotCalculator.JackpotPeriod.MONTHLY, JackpotCalculator.classifyPayoutAmount(3888.00));
        assertEquals(JackpotCalculator.JackpotPeriod.QUARTERLY, JackpotCalculator.classifyPayoutAmount(9720.00));
        assertEquals(JackpotCalculator.JackpotPeriod.HALF_YEAR, JackpotCalculator.classifyPayoutAmount(15724.80));
        assertEquals(JackpotCalculator.JackpotPeriod.YEARLY, JackpotCalculator.classifyPayoutAmount(23652.00));
    }

    @Test
    public void rejectsNonJackpotAndUnsafePayoutAmounts() {
        assertEquals(JackpotCalculator.JackpotPeriod.UNKNOWN, JackpotCalculator.classifyPayoutAmount(6.00));
        assertEquals(JackpotCalculator.JackpotPeriod.UNKNOWN, JackpotCalculator.classifyPayoutAmount(0.00));
        assertEquals(JackpotCalculator.JackpotPeriod.UNKNOWN, JackpotCalculator.classifyPayoutAmount(302.41));
        assertEquals(JackpotCalculator.JackpotPeriod.UNKNOWN, JackpotCalculator.classifyPayoutAmount(302.399));
        assertEquals(JackpotCalculator.JackpotPeriod.UNKNOWN, JackpotCalculator.classifyPayoutAmount(Double.NaN));
        assertEquals(JackpotCalculator.JackpotPeriod.UNKNOWN, JackpotCalculator.classifyPayoutAmount(Double.POSITIVE_INFINITY));
        assertFalse(JackpotCalculator.isCanonicalPayoutAmount(6.00));
        assertFalse(JackpotCalculator.isCanonicalPayoutAmount(302.399));
    }

    @Test
    public void payoutAmountMapIsUniqueAndDistinctFromMinerReward() {
        Map<JackpotCalculator.JackpotPeriod, BigDecimal> payouts = JackpotCalculator.allExpectedPayoutAmounts();

        assertEquals(new BigDecimal("302.40"), JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.DAILY));
        assertEquals(new BigDecimal("1209.60"), JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.WEEKLY));
        assertEquals(new BigDecimal("3888.00"), JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.MONTHLY));
        assertEquals(new BigDecimal("9720.00"), JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.QUARTERLY));
        assertEquals(new BigDecimal("15724.80"), JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.HALF_YEAR));
        assertEquals(new BigDecimal("23652.00"), JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.YEARLY));
        assertEquals(new BigDecimal("0.00"), JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.UNKNOWN));
        assertEquals(6, payouts.size());
        assertEquals(payouts.size(), new HashSet<>(payouts.values()).size());
        assertTrue(JackpotCalculator.validatePayoutAmountsAreUnique());
        assertTrue(JackpotCalculator.validateReserveSplit());
    }

    @Test
    public void periodBoundariesAreDeterministic() {
        long daily = JackpotCalculator.dailyWindowBlocks();

        assertEquals(0L, JackpotCalculator.periodStart(0L, daily));
        assertEquals(0L, JackpotCalculator.periodStart(431L, daily));
        assertEquals(432L, JackpotCalculator.periodStart(432L, daily));
        assertEquals(431L, JackpotCalculator.periodEnd(0L, daily));
        assertEquals(431L, JackpotCalculator.periodEnd(431L, daily));
        assertEquals(863L, JackpotCalculator.periodEnd(432L, daily));
        assertFalse(JackpotCalculator.isPeriodEnd(430L, daily));
        assertTrue(JackpotCalculator.isPeriodEnd(431L, daily));
        assertEquals(491L, JackpotCalculator.maturityBlock(431L, JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS));
    }

    @Test
    public void invalidPeriodInputsAreSafe() {
        assertEquals(-1L, JackpotCalculator.periodStart(-1L, 432L));
        assertEquals(-1L, JackpotCalculator.periodStart(1L, 0L));
        assertEquals(-1L, JackpotCalculator.periodEnd(-1L, 432L));
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
