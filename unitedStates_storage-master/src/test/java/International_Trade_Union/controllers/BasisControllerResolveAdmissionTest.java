package International_Trade_Union.controllers;

import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasisControllerResolveAdmissionTest {
    @Test
    public void v35DisabledKeepsLegacyResolveAdmission() {
        DataShortBlockchainInformation local = data(true, 10, 100, "1", "a");
        DataShortBlockchainInformation fakeWorkCandidate = data(true, 11, 100,
                "999999999999999999999999999999999999999999999999", "b");

        assertTrue(Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE);
        assertFalse(BasisController.shouldUseV35ResolveAdmission(local, fakeWorkCandidate, Seting.V35_RESCUE_FORK_INDEX));
        assertTrue(BasisController.shouldAcceptResolveCandidate(local, fakeWorkCandidate));
    }

    @Test
    public void hugeFakeCumulativeWorkBeforeV35DoesNotBypassLegacyAdmission() {
        DataShortBlockchainInformation local = data(true, 10, 100, "1", "a");
        DataShortBlockchainInformation fakeWorkButLegacyLosingCandidate = data(true, 11, 99,
                "999999999999999999999999999999999999999999999999", "b");

        assertFalse(BasisController.shouldUseV35ResolveAdmission(local, fakeWorkButLegacyLosingCandidate, Seting.V35_RESCUE_FORK_INDEX));
        assertFalse(BasisController.shouldAcceptResolveCandidate(local, fakeWorkButLegacyLosingCandidate));
    }

    @Test
    public void invalidCumulativeWorkDoesNotCrashOrWinAgainstValidV35Work() {
        DataShortBlockchainInformation local = data(true, 10, 100, "1000", "a");
        DataShortBlockchainInformation invalidCandidate = data(true, 11, 999999, "not-a-number", "b");

        assertTrue(BasisController.shouldUseV35ResolveAdmission(local, invalidCandidate, 10L));
        assertFalse(BasisController.shouldAcceptResolveCandidate(local, invalidCandidate, 10L));
    }

    @Test
    public void simulatedV35ResolveAdmissionPrefersHigherCumulativeWorkBeforeHashCount() {
        DataShortBlockchainInformation local = data(true, 10, 1_000_000, "1000", "a");
        DataShortBlockchainInformation candidate = data(true, 11, 1, "2000", "b");

        assertTrue(BasisController.shouldUseV35ResolveAdmission(local, candidate, 10L));
        assertTrue(BasisController.shouldAcceptResolveCandidate(local, candidate, 10L));
    }

    @Test
    public void simulatedV35ResolveAdmissionRejectsLowerWorkEvenWithHigherHashCountAndSize() {
        DataShortBlockchainInformation local = data(true, 10, 1, "2000", "a");
        DataShortBlockchainInformation candidate = data(true, 11, 1_000_000, "1000", "b");

        assertTrue(BasisController.shouldUseV35ResolveAdmission(local, candidate, 10L));
        assertFalse(BasisController.shouldAcceptResolveCandidate(local, candidate, 10L));
    }

    @Test
    public void simulatedV35ResolveAdmissionUsesSizeWhenCumulativeWorkIsEqual() {
        DataShortBlockchainInformation local = data(true, 10, 1, "2000", "a");
        DataShortBlockchainInformation candidate = data(true, 11, 0, "2000", "b");

        assertTrue(BasisController.shouldAcceptResolveCandidate(local, candidate, 10L));
    }

    @Test
    public void legacyV34MinimumDifficultyStillAppliesBeforeV35() {
        Block weakLegacyBlock = block(Seting.V34_MIN_DIFF - 1, 1L);

        assertTrue(BasisController.isResolveBlockBelowMinimumDifficulty(weakLegacyBlock, Long.MAX_VALUE));
    }

    @Test
    public void v35CompactTargetDoesNotUseLegacyV34MinimumDifficultyFloor() {
        Block v35BlockWithCompactTarget = block(Seting.V34_MIN_DIFF - 1, 10L);

        assertFalse(BasisController.isResolveBlockBelowMinimumDifficulty(v35BlockWithCompactTarget, 10L));
    }

    private static DataShortBlockchainInformation data(boolean valid,
                                                       long size,
                                                       long hashCount,
                                                       String cumulativeWork,
                                                       String tipHash) {
        return new DataShortBlockchainInformation(
                size,
                valid,
                hashCount,
                BigDecimal.ZERO,
                0L,
                0,
                cumulativeWork,
                tipHash);
    }

    private static Block block(long hashCompexity, long index) {
        return new Block(new ArrayList<>(), "previous", "miner", "founder",
                0L, 0.0, hashCompexity, new Timestamp(1_000L), index, "hash");
    }
}
