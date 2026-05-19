package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.setings.Seting;

/**
 * Immutable, transaction-format-neutral view of a possible V35 jackpot payout.
 */
public final class JackpotPayoutCandidate {
    private final long payoutBlockIndex;
    private final String sender;
    private final String recipient;
    private final double digitalDollar;
    private final double digitalStockBalance;
    private final double bonusForMiner;
    private final JackpotCalculator.JackpotPeriod periodType;

    public JackpotPayoutCandidate(long payoutBlockIndex,
                                  String sender,
                                  String recipient,
                                  double digitalDollar,
                                  double digitalStockBalance,
                                  double bonusForMiner) {
        this.payoutBlockIndex = payoutBlockIndex;
        this.sender = normalize(sender);
        this.recipient = normalize(recipient);
        this.digitalDollar = digitalDollar;
        this.digitalStockBalance = digitalStockBalance;
        this.bonusForMiner = bonusForMiner;
        this.periodType = JackpotCalculator.classifyPayoutAmount(digitalDollar);
    }

    public static JackpotPayoutCandidate fromTransaction(long payoutBlockIndex, DtoTransaction transaction) {
        if (transaction == null) {
            return new JackpotPayoutCandidate(payoutBlockIndex, "", "", 0.0, 0.0, 0.0);
        }
        return new JackpotPayoutCandidate(
                payoutBlockIndex,
                transaction.getSender(),
                transaction.getCustomer(),
                transaction.getDigitalDollar(),
                transaction.getDigitalStockBalance(),
                transaction.getBonusForMiner());
    }

    public long getPayoutBlockIndex() {
        return payoutBlockIndex;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public double getDigitalDollar() {
        return digitalDollar;
    }

    public double getDigitalStockBalance() {
        return digitalStockBalance;
    }

    public double getBonusForMiner() {
        return bonusForMiner;
    }

    public JackpotCalculator.JackpotPeriod getPeriodType() {
        return periodType;
    }

    public boolean isBasisPayout() {
        return Seting.BASIS_ADDRESS.equals(sender);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
