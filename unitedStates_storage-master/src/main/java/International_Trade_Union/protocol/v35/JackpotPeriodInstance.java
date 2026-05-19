package International_Trade_Union.protocol.v35;

/**
 * Deterministic jackpot period instance inferred from payout amount and block index.
 */
public final class JackpotPeriodInstance {
    private final JackpotCalculator.JackpotPeriod periodType;
    private final long periodStart;
    private final long periodEnd;
    private final long maturityBlock;
    private final long firstPayoutBlock;
    private final String duplicateKey;

    public JackpotPeriodInstance(JackpotCalculator.JackpotPeriod periodType,
                                 long periodStart,
                                 long periodEnd,
                                 long maturityBlock,
                                 long firstPayoutBlock) {
        this.periodType = periodType == null ? JackpotCalculator.JackpotPeriod.UNKNOWN : periodType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.maturityBlock = maturityBlock;
        this.firstPayoutBlock = firstPayoutBlock;
        this.duplicateKey = duplicateKey(this.periodType, periodStart, periodEnd);
    }

    public static JackpotPeriodInstance invalid(JackpotCalculator.JackpotPeriod periodType) {
        return new JackpotPeriodInstance(periodType, -1L, -1L, -1L, -1L);
    }

    public JackpotCalculator.JackpotPeriod getPeriodType() {
        return periodType;
    }

    public long getPeriodStart() {
        return periodStart;
    }

    public long getPeriodEnd() {
        return periodEnd;
    }

    public long getMaturityBlock() {
        return maturityBlock;
    }

    public long getFirstPayoutBlock() {
        return firstPayoutBlock;
    }

    public String getDuplicateKey() {
        return duplicateKey;
    }

    public boolean isValid() {
        return periodType != JackpotCalculator.JackpotPeriod.UNKNOWN
                && periodStart >= 0
                && periodEnd >= periodStart
                && maturityBlock > periodEnd
                && firstPayoutBlock > maturityBlock;
    }

    public static String duplicateKey(JackpotCalculator.JackpotPeriod periodType, long periodStart, long periodEnd) {
        String name = periodType == null ? JackpotCalculator.JackpotPeriod.UNKNOWN.name() : periodType.name();
        return "V35-JACKPOT:" + name + ":" + periodStart + ":" + periodEnd;
    }
}
