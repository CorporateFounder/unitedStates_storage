package International_Trade_Union.protocol.v35;

import java.util.Objects;

/**
 * Verifiable output of pure V35 jackpot winner selection. It contains no payout
 * information and does not imply that a BASIS transaction should be created.
 */
public final class JackpotWinnerProof {
    private final long periodStart;
    private final long periodEnd;
    private final long maturityBlock;
    private final int randomDelayBlocks;
    private final String seedHex;
    private final int selectedOffset;
    private final long selectedBlockIndex;
    private final String winnerMinerAddress;

    public JackpotWinnerProof(long periodStart, long periodEnd, long maturityBlock,
                              int randomDelayBlocks, String seedHex, int selectedOffset,
                              long selectedBlockIndex, String winnerMinerAddress) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.maturityBlock = maturityBlock;
        this.randomDelayBlocks = randomDelayBlocks;
        this.seedHex = seedHex == null ? "" : seedHex;
        this.selectedOffset = selectedOffset;
        this.selectedBlockIndex = selectedBlockIndex;
        this.winnerMinerAddress = winnerMinerAddress == null ? "" : winnerMinerAddress.trim();
    }

    public static JackpotWinnerProof noWinner(long periodStart, long periodEnd, long maturityBlock,
                                              int randomDelayBlocks, String seedHex) {
        return new JackpotWinnerProof(periodStart, periodEnd, maturityBlock, randomDelayBlocks,
                seedHex, -1, -1L, "");
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

    public int getRandomDelayBlocks() {
        return randomDelayBlocks;
    }

    public String getSeedHex() {
        return seedHex;
    }

    public int getSelectedOffset() {
        return selectedOffset;
    }

    public long getSelectedBlockIndex() {
        return selectedBlockIndex;
    }

    public String getWinnerMinerAddress() {
        return winnerMinerAddress;
    }

    public boolean hasWinner() {
        return selectedOffset >= 0 && selectedBlockIndex >= 0 && !winnerMinerAddress.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JackpotWinnerProof)) {
            return false;
        }
        JackpotWinnerProof that = (JackpotWinnerProof) o;
        return periodStart == that.periodStart
                && periodEnd == that.periodEnd
                && maturityBlock == that.maturityBlock
                && randomDelayBlocks == that.randomDelayBlocks
                && selectedOffset == that.selectedOffset
                && selectedBlockIndex == that.selectedBlockIndex
                && Objects.equals(seedHex, that.seedHex)
                && Objects.equals(winnerMinerAddress, that.winnerMinerAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodStart, periodEnd, maturityBlock, randomDelayBlocks,
                seedHex, selectedOffset, selectedBlockIndex, winnerMinerAddress);
    }
}
