package International_Trade_Union.protocol.v35;

public final class V35SupplyTransactionRow {
    private final String sender;
    private final double digitalDollar;
    private final long blockIndex;

    public V35SupplyTransactionRow(String sender, double digitalDollar, long blockIndex) {
        this.sender = sender;
        this.digitalDollar = digitalDollar;
        this.blockIndex = blockIndex;
    }

    public String getSender() {
        return sender;
    }

    public double getDigitalDollar() {
        return digitalDollar;
    }

    public long getBlockIndex() {
        return blockIndex;
    }
}
