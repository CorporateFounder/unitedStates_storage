package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35WorkCalculatorTest {
    @Test
    public void lowerTargetProducesHigherWork() {
        BigInteger lowerTargetWork = V35WorkCalculator.workFromTarget(BigInteger.valueOf(100));
        BigInteger higherTargetWork = V35WorkCalculator.workFromTarget(BigInteger.valueOf(1000));

        assertTrue(lowerTargetWork.compareTo(higherTargetWork) > 0);
    }

    @Test
    public void higherTargetProducesLowerWork() {
        BigInteger lowerTargetWork = V35WorkCalculator.workFromTarget(BigInteger.valueOf(10));
        BigInteger higherTargetWork = V35WorkCalculator.workFromTarget(BigInteger.valueOf(100));

        assertTrue(higherTargetWork.compareTo(lowerTargetWork) < 0);
    }

    @Test
    public void sameTargetProducesSameWork() {
        BigInteger first = V35WorkCalculator.workFromTarget(BigInteger.valueOf(500));
        BigInteger second = V35WorkCalculator.workFromTarget(BigInteger.valueOf(500));

        assertEquals(first, second);
    }

    @Test
    public void invalidTargetReturnsZero() {
        assertEquals(BigInteger.ZERO, V35WorkCalculator.workFromTarget(null));
        assertEquals(BigInteger.ZERO, V35WorkCalculator.workFromTarget(BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, V35WorkCalculator.workFromTarget(BigInteger.valueOf(-1)));
    }

    @Test
    public void compactTargetWorkIsPositiveForValidCompactTarget() {
        assertTrue(V35WorkCalculator.workFromCompactTarget(V35TargetDifficulty.getInitialCompactTarget()).signum() > 0);
    }

    @Test
    public void cumulativeWorkSumsCompactTargets() {
        long compact = V35TargetDifficulty.getInitialCompactTarget();
        BigInteger oneBlockWork = V35WorkCalculator.workFromCompactTarget(compact);

        assertEquals(
                oneBlockWork.multiply(BigInteger.valueOf(3)),
                V35WorkCalculator.cumulativeWorkFromCompactTargets(Arrays.asList(compact, compact, compact)));
    }

    @Test
    public void invalidCompactTargetsContributeZero() {
        long compact = V35TargetDifficulty.getInitialCompactTarget();

        assertEquals(
                V35WorkCalculator.workFromCompactTarget(compact),
                V35WorkCalculator.cumulativeWorkFromCompactTargets(Arrays.asList(0L, compact, null, -1L)));
    }
}
