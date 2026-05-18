package International_Trade_Union.protocol.v35;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Pure V35 chain scoring helper. This class is not wired to sync or tournament code.
 */
public final class ChainTournamentScorer {
    private static final double MIN_TIE_BREAKER_SCORE = 0.0;
    private static final double MAX_TIE_BREAKER_SCORE = 1.0;

    public static final String INTENDED_PRIORITY =
            "1. valid chain; 2. highest cumulativeWork; 3. latest valid height; "
                    + "4. capped stake/tx tie-breaker; 5. deterministic hash tie-breaker";

    private ChainTournamentScorer() {
    }

    public static String intendedPriority() {
        return INTENDED_PRIORITY;
    }

    public static double capTieBreakerScore(double score) {
        if (Double.isNaN(score) || score <= MIN_TIE_BREAKER_SCORE) {
            return MIN_TIE_BREAKER_SCORE;
        }
        if (Double.isInfinite(score) || score > MAX_TIE_BREAKER_SCORE) {
            return MAX_TIE_BREAKER_SCORE;
        }
        return score;
    }

    public static ChainScore score(boolean valid,
                                   BigInteger cumulativeWork,
                                   long height,
                                   double cappedTieBreakerScore,
                                   String tipHash) {
        return new ChainScore(valid, cumulativeWork, height, cappedTieBreakerScore, tipHash);
    }

    public static int compare(ChainScore left, ChainScore right) {
        ChainScore safeLeft = left == null ? ChainScore.invalid() : left;
        ChainScore safeRight = right == null ? ChainScore.invalid() : right;

        int validCompare = Boolean.compare(safeLeft.isValid(), safeRight.isValid());
        if (validCompare != 0) {
            return validCompare;
        }

        int workCompare = safeLeft.getCumulativeWork().compareTo(safeRight.getCumulativeWork());
        if (workCompare != 0) {
            return workCompare;
        }

        int heightCompare = Long.compare(safeLeft.getHeight(), safeRight.getHeight());
        if (heightCompare != 0) {
            return heightCompare;
        }

        int tieBreakerCompare = Double.compare(safeLeft.getCappedTieBreakerScore(), safeRight.getCappedTieBreakerScore());
        if (tieBreakerCompare != 0) {
            return tieBreakerCompare;
        }

        return compareTipHash(safeLeft.getTipHash(), safeRight.getTipHash());
    }

    public static ChainScore betterOf(ChainScore left, ChainScore right) {
        ChainScore safeLeft = left == null ? ChainScore.invalid() : left;
        ChainScore safeRight = right == null ? ChainScore.invalid() : right;
        return compare(safeLeft, safeRight) >= 0 ? safeLeft : safeRight;
    }

    private static int compareTipHash(String leftTipHash, String rightTipHash) {
        boolean leftEmpty = leftTipHash == null || leftTipHash.isEmpty();
        boolean rightEmpty = rightTipHash == null || rightTipHash.isEmpty();
        if (leftEmpty && rightEmpty) {
            return 0;
        }
        if (leftEmpty) {
            return -1;
        }
        if (rightEmpty) {
            return 1;
        }
        return rightTipHash.compareTo(leftTipHash);
    }

    public static final class ChainScore {
        private static final ChainScore INVALID = new ChainScore(false, BigInteger.ZERO, 0L, 0.0, "");

        private final boolean valid;
        private final BigInteger cumulativeWork;
        private final long height;
        private final double cappedTieBreakerScore;
        private final String tipHash;

        private ChainScore(boolean valid,
                           BigInteger cumulativeWork,
                           long height,
                           double cappedTieBreakerScore,
                           String tipHash) {
            this.valid = valid;
            this.cumulativeWork = sanitizeWork(cumulativeWork);
            this.height = Math.max(0L, height);
            this.cappedTieBreakerScore = capTieBreakerScore(cappedTieBreakerScore);
            this.tipHash = tipHash == null ? "" : tipHash;
        }

        public static ChainScore invalid() {
            return INVALID;
        }

        public boolean isValid() {
            return valid;
        }

        public BigInteger getCumulativeWork() {
            return cumulativeWork;
        }

        public long getHeight() {
            return height;
        }

        public double getCappedTieBreakerScore() {
            return cappedTieBreakerScore;
        }

        public String getTipHash() {
            return tipHash;
        }

        private static BigInteger sanitizeWork(BigInteger cumulativeWork) {
            if (cumulativeWork == null || cumulativeWork.signum() <= 0) {
                return BigInteger.ZERO;
            }
            return cumulativeWork;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ChainScore)) {
                return false;
            }
            ChainScore that = (ChainScore) o;
            return valid == that.valid
                    && height == that.height
                    && Double.compare(that.cappedTieBreakerScore, cappedTieBreakerScore) == 0
                    && Objects.equals(cumulativeWork, that.cumulativeWork)
                    && Objects.equals(tipHash, that.tipHash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(valid, cumulativeWork, height, cappedTieBreakerScore, tipHash);
        }
    }
}
