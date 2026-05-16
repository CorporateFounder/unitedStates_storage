package International_Trade_Union.model.comparator;

import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.model.HostEndDataShortB;
import International_Trade_Union.protocol.v35.ChainTournamentScorer;
import International_Trade_Union.setings.Seting;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

public class HostEndDataShortBComparator implements Comparator<HostEndDataShortB> {

    @Override
    public int compare(HostEndDataShortB h1, HostEndDataShortB h2) {
        DataShortBlockchainInformation d1 = h1.getDataShortBlockchainInformation();
        DataShortBlockchainInformation d2 = h2.getDataShortBlockchainInformation();

        if (shouldUseV35Comparison(d1, d2)) {
            return -compareV35(d1, d2);
        }

        if (d1.getSize() != d1.getSize()) {
            return Long.compare(d2.getSize(), d1.getSize());
        }

        if (d1.getBigRandomNumber() != d2.getBigRandomNumber()) {
            return Integer.compare(d2.getBigRandomNumber(), d1.getBigRandomNumber());
        }

        return Long.compare(d2.getTransactions(), d1.getTransactions());
    }

    public static boolean shouldUseV35Comparison(DataShortBlockchainInformation d1, DataShortBlockchainInformation d2) {
        if (Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE) {
            return false;
        }
        if (!isAtOrAfterV35(d1) && !isAtOrAfterV35(d2)) {
            return false;
        }
        return parseCumulativeWork(d1).signum() > 0 || parseCumulativeWork(d2).signum() > 0;
    }

    private static boolean isAtOrAfterV35(DataShortBlockchainInformation data) {
        return data != null && data.getSize() >= Seting.V35_RESCUE_FORK_INDEX;
    }

    public static int compareV35(DataShortBlockchainInformation d1, DataShortBlockchainInformation d2) {
        return ChainTournamentScorer.compare(toChainScore(d1), toChainScore(d2));
    }

    private static ChainTournamentScorer.ChainScore toChainScore(DataShortBlockchainInformation data) {
        if (data == null) {
            return ChainTournamentScorer.ChainScore.invalid();
        }
        return ChainTournamentScorer.score(
                data.isValidation(),
                parseCumulativeWork(data),
                data.getSize(),
                tieBreakerScore(data),
                data.getTipHash());
    }

    private static BigInteger parseCumulativeWork(DataShortBlockchainInformation data) {
        if (data == null || data.getCumulativeWork() == null || data.getCumulativeWork().trim().isEmpty()) {
            return BigInteger.ZERO;
        }
        try {
            BigInteger parsed = new BigInteger(data.getCumulativeWork());
            return parsed.signum() <= 0 ? BigInteger.ZERO : parsed;
        } catch (NumberFormatException e) {
            return BigInteger.ZERO;
        }
    }

    private static double tieBreakerScore(DataShortBlockchainInformation data) {
        double stake = normalized(data.getStaking());
        double transactions = normalized(data.getTransactions());
        double random = normalized(data.getBigRandomNumber());
        return ChainTournamentScorer.capTieBreakerScore((stake + transactions + random) / 3.0);
    }

    private static double normalized(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        double number = value.doubleValue();
        if (Double.isInfinite(number) || Double.isNaN(number)) {
            return 1.0;
        }
        return number / (number + 1.0);
    }

    private static double normalized(long value) {
        if (value <= 0) {
            return 0.0;
        }
        return (double) value / ((double) value + 1.0);
    }
}
