package International_Trade_Union.protocol.v35;

import java.math.BigDecimal;
import java.time.Instant;

public final class V35SupplyContext {
    private final V35SupplySnapshot snapshot;
    private final long currentHeight;
    private final String latestBlockHash;
    private final Instant createdAt;
    private final String source;
    private final boolean stale;
    private final long lastProcessedHeight;
    private final long rowsScanned;
    private final int pagesProcessed;

    public V35SupplyContext(V35SupplySnapshot snapshot,
                            long currentHeight,
                            String latestBlockHash,
                            Instant createdAt,
                            String source,
                            boolean stale,
                            long lastProcessedHeight,
                            long rowsScanned,
                            int pagesProcessed) {
        this.snapshot = snapshot;
        this.currentHeight = currentHeight;
        this.latestBlockHash = normalize(latestBlockHash);
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.source = source == null ? "bounded_projection_read_model" : source;
        this.stale = stale;
        this.lastProcessedHeight = lastProcessedHeight;
        this.rowsScanned = rowsScanned;
        this.pagesProcessed = pagesProcessed;
    }

    public V35SupplySnapshot getSnapshot() {
        return snapshot;
    }

    public long getCurrentHeight() {
        return currentHeight;
    }

    public String getLatestBlockHash() {
        return latestBlockHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getSource() {
        return source;
    }

    public boolean isStale() {
        return stale;
    }

    public long getLastProcessedHeight() {
        return lastProcessedHeight;
    }

    public long getRowsScanned() {
        return rowsScanned;
    }

    public int getPagesProcessed() {
        return pagesProcessed;
    }

    public BigDecimal getTotalMintedGold() {
        return snapshot.getTotalMintedGold();
    }

    public BigDecimal getJackpotPaidGold() {
        return snapshot.getJackpotPaidGold();
    }

    public V35SupplyContext asStale(String staleSource) {
        return new V35SupplyContext(
                snapshot,
                currentHeight,
                latestBlockHash,
                createdAt,
                staleSource,
                true,
                lastProcessedHeight,
                rowsScanned,
                pagesProcessed);
    }

    private static String normalize(String value) {
        return value == null || value.trim().isEmpty() ? "UNKNOWN" : value.trim();
    }
}
