package International_Trade_Union.protocol.v35;

import java.util.Objects;

/**
 * Pure V35 jackpot ticket model. One eligible mined block is one ticket in
 * Phase 4C; weight is capped for future weighted selection work.
 */
public final class JackpotTicket {
    private final long blockIndex;
    private final String minerAddress;
    private final double weight;

    public JackpotTicket(long blockIndex, String minerAddress, double weight) {
        this.blockIndex = blockIndex;
        this.minerAddress = minerAddress == null ? "" : minerAddress.trim();
        this.weight = sanitizeWeight(weight);
    }

    public static JackpotTicket of(long blockIndex, String minerAddress) {
        return new JackpotTicket(blockIndex, minerAddress, TicketWeightCalculator.baseWeight());
    }

    public long getBlockIndex() {
        return blockIndex;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isValid() {
        return blockIndex >= 0 && !minerAddress.isEmpty() && weight > 0.0;
    }

    private static double sanitizeWeight(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0.0) {
            return 0.0;
        }
        return Math.min(value, TicketWeightCalculator.maxWeight());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JackpotTicket)) {
            return false;
        }
        JackpotTicket that = (JackpotTicket) o;
        return blockIndex == that.blockIndex
                && Double.compare(that.weight, weight) == 0
                && Objects.equals(minerAddress, that.minerAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockIndex, minerAddress, weight);
    }
}
