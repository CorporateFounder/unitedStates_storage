package International_Trade_Union.protocol.v35;

import java.util.Objects;

public final class V35ChainTip {
    private final long height;
    private final String blockHash;

    public V35ChainTip(long height, String blockHash) {
        this.height = height;
        this.blockHash = normalize(blockHash);
    }

    public long getHeight() {
        return height;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public boolean matches(V35SupplyContext context) {
        return context != null
                && height == context.getCurrentHeight()
                && blockHash.equals(context.getLatestBlockHash());
    }

    private static String normalize(String value) {
        return value == null || value.trim().isEmpty() ? "UNKNOWN" : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof V35ChainTip)) {
            return false;
        }
        V35ChainTip that = (V35ChainTip) other;
        return height == that.height && blockHash.equals(that.blockHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, blockHash);
    }
}
