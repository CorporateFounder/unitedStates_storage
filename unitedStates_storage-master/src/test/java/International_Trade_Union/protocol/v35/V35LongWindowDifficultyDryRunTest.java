package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35LongWindowDifficultyDryRunTest {
    private static final long SIMULATED_FORK_INDEX = 6_000L;
    private static final int EXTENDED_WINDOWS = 12;

    @Test
    public void productionConstantsRemainDisabledAndV35TimingConstantsMatchPolicy() {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertEquals(200, V35TargetDifficulty.getTargetBlockTimeSeconds());
        assertEquals(432, 86_400 / V35TargetDifficulty.getTargetBlockTimeSeconds());
        assertFalse(ProtocolVersion.isV35(SIMULATED_FORK_INDEX));

        assertFalse(V35JackpotProofService.isV35ActiveAt(SIMULATED_FORK_INDEX - 1L, SIMULATED_FORK_INDEX));
        assertTrue(V35JackpotProofService.isV35ActiveAt(SIMULATED_FORK_INDEX, SIMULATED_FORK_INDEX));
    }

    @Test
    public void perfectTwoHundredSecondWindowsKeepTargetStableAcrossExtendedRun() {
        long compact = V35TargetDifficulty.getInitialCompactTarget();
        BigInteger initialTarget = V35TargetDifficulty.decodeCompactTarget(compact);
        long expected = expectedWindowSeconds();

        for (int i = 0; i < EXTENDED_WINDOWS; i++) {
            compact = V35TargetDifficulty.calculateNextCompactTarget(
                    compact,
                    expected,
                    expected,
                    V35TargetDifficulty.getTargetBlockTimeSeconds());
            BigInteger target = V35TargetDifficulty.decodeCompactTarget(compact);

            assertEquals(initialTarget, target);
            assertTrue(target.signum() > 0);
            assertTrue(target.bitLength() <= 256);
        }
    }

    @Test
    public void tooFastWindowsMakeTargetHarderAndIncreaseCumulativeWork() {
        List<Long> compactTargets = runAdjustmentWindows(expectedWindowSeconds() / 2L,
                V35TargetDifficulty.getTargetBlockTimeSeconds());

        BigInteger previousTarget = V35TargetDifficulty.decodeCompactTarget(compactTargets.get(0));
        BigInteger previousWork = V35WorkCalculator.workFromCompactTarget(compactTargets.get(0));
        for (int i = 1; i < compactTargets.size(); i++) {
            BigInteger target = V35TargetDifficulty.decodeCompactTarget(compactTargets.get(i));
            BigInteger work = V35WorkCalculator.workFromCompactTarget(compactTargets.get(i));

            assertTrue(target.signum() > 0);
            assertTrue(target.compareTo(previousTarget) <= 0);
            assertTrue(work.compareTo(previousWork) >= 0);
            assertTrue(V35PowValidator.isHashUnderTarget(target.toString(16), target));
            assertFalse(V35PowValidator.isHashUnderTarget(target.add(BigInteger.ONE).toString(16), target));

            previousTarget = target;
            previousWork = work;
        }

        BigInteger cumulative = V35WorkCalculator.cumulativeWorkFromCompactTargets(compactTargets);
        assertTrue(cumulative.compareTo(previousWork) > 0);
    }

    @Test
    public void tooSlowWindowsMakeTargetEasierButRemainBoundedAndPositive() {
        List<Long> compactTargets = runAdjustmentWindows(expectedWindowSeconds() * 2L,
                V35TargetDifficulty.getTargetBlockTimeSeconds() * 2L);

        BigInteger previousTarget = V35TargetDifficulty.decodeCompactTarget(compactTargets.get(0));
        BigInteger previousWork = V35WorkCalculator.workFromCompactTarget(compactTargets.get(0));
        for (int i = 1; i < compactTargets.size(); i++) {
            BigInteger target = V35TargetDifficulty.decodeCompactTarget(compactTargets.get(i));
            BigInteger work = V35WorkCalculator.workFromCompactTarget(compactTargets.get(i));
            BigInteger maxAllowed = V35TargetDifficulty.clampTargetChange(previousTarget, previousTarget.shiftLeft(8));

            assertTrue(target.signum() > 0);
            assertTrue(target.bitLength() <= 256);
            assertTrue(target.compareTo(V35TargetDifficulty.getInitialTarget()) <= 0);
            assertTrue(target.compareTo(previousTarget) >= 0);
            assertTrue(target.compareTo(maxAllowed) <= 0);
            assertTrue(work.compareTo(previousWork) <= 0);

            previousTarget = target;
            previousWork = work;
        }
    }

    @Test
    public void emergencyEasingIsBoundedAndNeverProducesInvalidTarget() {
        long compact = V35TargetDifficulty.getInitialCompactTarget();
        long expected = expectedWindowSeconds();

        for (int i = 0; i < EXTENDED_WINDOWS; i++) {
            BigInteger previousTarget = V35TargetDifficulty.decodeCompactTarget(compact);
            compact = V35TargetDifficulty.calculateNextCompactTarget(
                    compact,
                    expected,
                    expected,
                    V35TargetDifficulty.getEmergencyDelaySeconds());
            BigInteger easedTarget = V35TargetDifficulty.decodeCompactTarget(compact);
            BigInteger maxAllowed = V35TargetDifficulty.clampTargetChange(previousTarget, previousTarget.shiftLeft(8));

            assertTrue(easedTarget.signum() > 0);
            assertTrue(easedTarget.compareTo(previousTarget) >= 0);
            assertTrue(easedTarget.compareTo(maxAllowed) <= 0);
            assertTrue(easedTarget.compareTo(V35TargetDifficulty.getInitialTarget()) <= 0);
            assertTrue(easedTarget.bitLength() <= 256);
        }
    }

    @Test
    public void compactTargetsRoundTripAndInvalidInputsStaySafeDuringDryRun() {
        for (Long compact : runAdjustmentWindows(expectedWindowSeconds(), V35TargetDifficulty.getTargetBlockTimeSeconds())) {
            BigInteger target = V35TargetDifficulty.decodeCompactTarget(compact);
            long roundTrip = V35TargetDifficulty.encodeCompactTarget(target);

            assertEquals(target, V35TargetDifficulty.decodeCompactTarget(roundTrip));
            assertTrue(target.signum() > 0);
            assertTrue(V35WorkCalculator.workFromCompactTarget(compact).signum() > 0);
        }

        assertEquals(BigInteger.ZERO, V35TargetDifficulty.decodeCompactTarget(0L));
        assertEquals(0L, V35TargetDifficulty.encodeCompactTarget(BigInteger.ZERO));
        assertEquals(0L, V35TargetDifficulty.encodeCompactTarget(BigInteger.valueOf(-1L)));
        assertEquals(V35TargetDifficulty.getInitialCompactTarget(),
                V35TargetDifficulty.calculateNextCompactTarget(0L, expectedWindowSeconds(), expectedWindowSeconds(), 0L));
    }

    private static List<Long> runAdjustmentWindows(long actualTimespanSeconds, long secondsSinceLastBlock) {
        List<Long> compactTargets = new ArrayList<>();
        long compact = V35TargetDifficulty.getInitialCompactTarget();
        compactTargets.add(compact);

        for (int i = 0; i < EXTENDED_WINDOWS; i++) {
            compact = V35TargetDifficulty.calculateNextCompactTarget(
                    compact,
                    actualTimespanSeconds,
                    expectedWindowSeconds(),
                    secondsSinceLastBlock);
            compactTargets.add(compact);
        }
        return compactTargets;
    }

    private static long expectedWindowSeconds() {
        return (long) V35TargetDifficulty.getTargetWindow() * V35TargetDifficulty.getTargetBlockTimeSeconds();
    }
}
