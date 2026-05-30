package International_Trade_Union.model.comparator;

import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.model.HostEndDataShortB;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsResolving;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HostEndDataShortBComparatorTest {
    private final HostEndDataShortBComparator comparator = new HostEndDataShortBComparator();

    @Test
    public void zeroCumulativeWorkUsesLegacyFallback() {
        DataShortBlockchainInformation highRandom = data(true, 10, 100, BigDecimal.ZERO, 1, 90, "0", "b");
        DataShortBlockchainInformation lowRandom = data(true, 100, 100, BigDecimal.ZERO, 999, 10, "0", "a");

        assertFalse(HostEndDataShortBComparator.shouldUseV35Comparison(highRandom, lowRandom));
        assertTrue(comparator.compare(host("high", highRandom), host("low", lowRandom)) < 0);
    }

    @Test
    public void oldDtoWithoutCumulativeWorkDoesNotCrash() {
        DataShortBlockchainInformation first = new DataShortBlockchainInformation();
        DataShortBlockchainInformation second = new DataShortBlockchainInformation();

        comparator.compare(host("first", first), host("second", second));
        assertFalse(HostEndDataShortBComparator.shouldUseV35Comparison(first, second));
    }

    @Test
    public void positiveCumulativeWorkDoesNotActivateWhileV35Disabled() {
        DataShortBlockchainInformation higherWork = data(true, 10, 1, BigDecimal.ZERO, 0, 0, "100", "ffff");
        DataShortBlockchainInformation legacyBetter = data(true, 10, 1, BigDecimal.valueOf(999999), 999999, 999999, "10", "0001");

        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertFalse(HostEndDataShortBComparator.shouldUseV35Comparison(higherWork, legacyBetter));
        assertTrue(comparator.compare(host("higher", higherWork), host("legacyBetter", legacyBetter)) > 0);
    }

    @Test
    public void hugeCumulativeWorkBelowV35DoesNotWin() {
        DataShortBlockchainInformation malicious = data(true, 10, 1, BigDecimal.ZERO, 0, 0,
                "999999999999999999999999999999999999999999999", "ffff");
        DataShortBlockchainInformation legacyBetter = data(true, 10, 1, BigDecimal.ZERO, 1, 100, "1", "0001");

        assertFalse(HostEndDataShortBComparator.shouldUseV35Comparison(malicious, legacyBetter));
        assertTrue(comparator.compare(host("malicious", malicious), host("legacyBetter", legacyBetter)) > 0);
    }

    @Test
    public void directV35ComparisonHelperStillPrioritizesCumulativeWork() {
        DataShortBlockchainInformation higherWork = data(true, 10, 1, BigDecimal.ZERO, 0, 0, "100", "ffff");
        DataShortBlockchainInformation noisyLowerWork = data(true, 10, 1, BigDecimal.valueOf(999999), 999999, 999999, "10", "0001");

        assertTrue(HostEndDataShortBComparator.compareV35(higherWork, noisyLowerWork) > 0);
    }

    @Test
    public void invalidCumulativeWorkFallsBackSafelyWhenNoPositiveWorkExists() {
        DataShortBlockchainInformation invalidWork = data(true, 10, 1, BigDecimal.ZERO, 0, 90, "not-a-number", "a");
        DataShortBlockchainInformation zeroWork = data(true, 10, 1, BigDecimal.ZERO, 0, 10, "0", "b");

        assertFalse(HostEndDataShortBComparator.shouldUseV35Comparison(invalidWork, zeroWork));
        assertTrue(comparator.compare(host("invalidWork", invalidWork), host("zeroWork", zeroWork)) < 0);
    }

    @Test
    public void utilsResolvingUsesLegacyWhenV35DisabledEvenWithPositiveWork() {
        DataShortBlockchainInformation actual = data(true, 10, 1, BigDecimal.valueOf(999), 999, 0, "1000", "b");
        DataShortBlockchainInformation global = data(true, 10, 1, BigDecimal.ZERO, 0, 100, "1", "a");

        assertFalse(HostEndDataShortBComparator.shouldUseV35Comparison(actual, global));
        assertTrue(new UtilsResolving().isBig(actual, global));
    }

    private static HostEndDataShortB host(String host, DataShortBlockchainInformation data) {
        return new HostEndDataShortB(host, data);
    }

    private static DataShortBlockchainInformation data(boolean valid,
                                                       long size,
                                                       long hashCount,
                                                       BigDecimal staking,
                                                       long transactions,
                                                       int bigRandomNumber,
                                                       String cumulativeWork,
                                                       String tipHash) {
        return new DataShortBlockchainInformation(size, valid, hashCount, staking, transactions, bigRandomNumber, cumulativeWork, tipHash);
    }
}
