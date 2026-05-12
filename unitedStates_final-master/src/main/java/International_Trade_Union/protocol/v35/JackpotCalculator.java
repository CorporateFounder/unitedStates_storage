package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * V35 jackpot split constants only. Winner selection and payouts are future work.
 */
public final class JackpotCalculator {
    private JackpotCalculator() {
    }

    public static double getDailyPerBlock() {
        return Seting.V35_DAILY_JACKPOT_PER_BLOCK;
    }

    public static double getWeeklyPerBlock() {
        return Seting.V35_WEEKLY_JACKPOT_PER_BLOCK;
    }

    public static double getMonthlyPerBlock() {
        return Seting.V35_MONTHLY_JACKPOT_PER_BLOCK;
    }

    public static double getQuarterlyPerBlock() {
        return Seting.V35_QUARTERLY_JACKPOT_PER_BLOCK;
    }

    public static double getHalfYearPerBlock() {
        return Seting.V35_HALF_YEAR_JACKPOT_PER_BLOCK;
    }

    public static double getYearlyPerBlock() {
        return Seting.V35_YEARLY_JACKPOT_PER_BLOCK;
    }

    public static double getTotalPerBlock() {
        return getDailyPerBlock()
                + getWeeklyPerBlock()
                + getMonthlyPerBlock()
                + getQuarterlyPerBlock()
                + getHalfYearPerBlock()
                + getYearlyPerBlock();
    }

    public static boolean validateSplit() {
        return Math.abs(getTotalPerBlock() - Seting.V35_JACKPOT_REWARD) < 0.00000001;
    }
}
