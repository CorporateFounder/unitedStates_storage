package International_Trade_Union.protocol.v35;

import java.math.BigDecimal;

/**
 * Pure result object for V35 jackpot payout proof validation.
 */
public final class JackpotProofResult {
    private final boolean valid;
    private final String reason;
    private final JackpotWinnerProof winnerProof;
    private final JackpotPeriodInstance periodInstance;
    private final BigDecimal expectedAmount;
    private final String expectedWinnerAddress;
    private final String actualRecipient;

    private JackpotProofResult(boolean valid,
                               String reason,
                               JackpotWinnerProof winnerProof,
                               JackpotPeriodInstance periodInstance,
                               BigDecimal expectedAmount,
                               String expectedWinnerAddress,
                               String actualRecipient) {
        this.valid = valid;
        this.reason = reason == null ? "" : reason;
        this.winnerProof = winnerProof;
        this.periodInstance = periodInstance;
        this.expectedAmount = expectedAmount;
        this.expectedWinnerAddress = expectedWinnerAddress == null ? "" : expectedWinnerAddress.trim();
        this.actualRecipient = actualRecipient == null ? "" : actualRecipient.trim();
    }

    public static JackpotProofResult valid(JackpotWinnerProof winnerProof,
                                           JackpotPeriodInstance periodInstance,
                                           BigDecimal expectedAmount,
                                           String actualRecipient) {
        return new JackpotProofResult(true, "valid", winnerProof, periodInstance, expectedAmount,
                winnerProof == null ? "" : winnerProof.getWinnerMinerAddress(), actualRecipient);
    }

    public static JackpotProofResult invalid(String reason,
                                             JackpotWinnerProof winnerProof,
                                             JackpotPeriodInstance periodInstance,
                                             BigDecimal expectedAmount,
                                             String actualRecipient) {
        return new JackpotProofResult(false, reason, winnerProof, periodInstance, expectedAmount,
                winnerProof == null ? "" : winnerProof.getWinnerMinerAddress(), actualRecipient);
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    public JackpotWinnerProof getWinnerProof() {
        return winnerProof;
    }

    public JackpotPeriodInstance getPeriodInstance() {
        return periodInstance;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public String getExpectedWinnerAddress() {
        return expectedWinnerAddress;
    }

    public String getActualRecipient() {
        return actualRecipient;
    }
}
