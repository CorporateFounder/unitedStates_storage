package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * Pure V35 ticket-weight cap helper. It does not alter balances or transactions.
 */
public final class TicketWeightCalculator {
    private TicketWeightCalculator() {
    }

    public static double baseWeight() {
        return Seting.V35_BASE_TICKET_WEIGHT;
    }

    public static double maxWeight() {
        return Seting.V35_MAX_TICKET_WEIGHT;
    }

    public static double capTxBoost(double txBoost) {
        return Math.max(0.00, Math.min(txBoost, Seting.V35_MAX_TX_BOOST));
    }

    public static double capStakeBoost(double stakeBoost) {
        return Math.max(0.00, Math.min(stakeBoost, Seting.V35_MAX_STAKE_BOOST));
    }

    public static double combine(double txBoost, double stakeBoost) {
        double combined = baseWeight() + capTxBoost(txBoost) + capStakeBoost(stakeBoost);
        return Math.min(combined, maxWeight());
    }
}
