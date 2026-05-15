package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35TargetDifficultyTest {
    @Test
    public void exposesTargetDifficultyConstants() {
        assertEquals(60, V35TargetDifficulty.getTargetWindow());
        assertEquals(150, V35TargetDifficulty.getTargetBlockTimeSeconds());
        assertEquals(1800L, V35TargetDifficulty.getEmergencyDelaySeconds());
    }

    @Test
    public void exposesInitialMiningTarget() {
        long compact = V35TargetDifficulty.getInitialCompactTarget();
        BigInteger target = V35TargetDifficulty.getInitialTarget();

        assertEquals(0x207fffffL, compact);
        assertEquals(target, V35TargetDifficulty.decodeCompactTarget(compact));
        assertTrue(target.signum() > 0);
    }

    @Test
    public void compactTargetRoundTripsSafeTargets() {
        BigInteger[] targets = {
                BigInteger.valueOf(0x123456L),
                new BigInteger("1234560000", 16),
                new BigInteger("1234560000000000000000000000000000000000000000000000000000", 16)
        };

        for (BigInteger target : targets) {
            long compact = V35TargetDifficulty.encodeCompactTarget(target);
            assertEquals(target, V35TargetDifficulty.decodeCompactTarget(compact));
        }
    }

    @Test
    public void calculateNextTargetUsesExpectedTimespan() {
        BigInteger previous = BigInteger.valueOf(1000);
        long expected = 60L * 150L;

        assertEquals(previous, V35TargetDifficulty.calculateNextTarget(previous, expected, expected));
        assertEquals(BigInteger.valueOf(1250), V35TargetDifficulty.calculateNextTarget(previous, expected * 2, expected));
        assertEquals(BigInteger.valueOf(500), V35TargetDifficulty.calculateNextTarget(previous, expected / 4, expected));
    }

    @Test
    public void clampPreventsOversizedMoves() {
        BigInteger previous = BigInteger.valueOf(1000);

        assertEquals(BigInteger.valueOf(1250), V35TargetDifficulty.clampTargetChange(previous, BigInteger.valueOf(10_000)));
        assertEquals(BigInteger.valueOf(500), V35TargetDifficulty.clampTargetChange(previous, BigInteger.valueOf(10)));
    }

    @Test
    public void calculateNextCompactTargetAdjustsAndClamps() {
        long compact = V35TargetDifficulty.getInitialCompactTarget();
        BigInteger previous = V35TargetDifficulty.decodeCompactTarget(compact);
        long expected = 60L * 150L;

        BigInteger stable = V35TargetDifficulty.decodeCompactTarget(
                V35TargetDifficulty.calculateNextCompactTarget(compact, expected, expected, 0L));
        BigInteger slow = V35TargetDifficulty.decodeCompactTarget(
                V35TargetDifficulty.calculateNextCompactTarget(compact, expected * 2, expected, 0L));
        BigInteger fast = V35TargetDifficulty.decodeCompactTarget(
                V35TargetDifficulty.calculateNextCompactTarget(compact, expected / 4, expected, 0L));

        assertEquals(previous, stable);
        assertTrue(slow.compareTo(previous) > 0);
        assertTrue(slow.compareTo(V35TargetDifficulty.clampTargetChange(previous, previous.shiftLeft(8))) <= 0);
        assertTrue(fast.compareTo(previous) < 0);
        assertTrue(fast.signum() > 0);
    }

    @Test
    public void emergencyDropOnlyAppliesAfterDelay() {
        BigInteger current = BigInteger.valueOf(1000);

        assertEquals(current, V35TargetDifficulty.applyEmergencyDrop(current, 1799L));
        BigInteger eased = V35TargetDifficulty.applyEmergencyDrop(current, 1800L);
        assertTrue(eased.compareTo(current) > 0);
        assertEquals(BigInteger.valueOf(1250), eased);
    }

    @Test
    public void compactEmergencyEasesOnlyAfterDelay() {
        long compact = V35TargetDifficulty.getInitialCompactTarget();
        BigInteger previous = V35TargetDifficulty.decodeCompactTarget(compact);
        long expected = 60L * 150L;

        BigInteger beforeDelay = V35TargetDifficulty.decodeCompactTarget(
                V35TargetDifficulty.calculateNextCompactTarget(compact, expected, expected, 1799L));
        BigInteger afterDelay = V35TargetDifficulty.decodeCompactTarget(
                V35TargetDifficulty.calculateNextCompactTarget(compact, expected, expected, 1800L));

        assertEquals(previous, beforeDelay);
        assertTrue(afterDelay.compareTo(previous) > 0);
    }
}
