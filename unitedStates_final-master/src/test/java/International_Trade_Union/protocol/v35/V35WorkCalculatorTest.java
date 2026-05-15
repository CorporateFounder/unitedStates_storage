package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

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
    public void invalidTargetReturnsZero() {
        assertEquals(BigInteger.ZERO, V35WorkCalculator.workFromTarget(null));
        assertEquals(BigInteger.ZERO, V35WorkCalculator.workFromTarget(BigInteger.ZERO));
        assertEquals(BigInteger.ZERO, V35WorkCalculator.workFromTarget(BigInteger.valueOf(-1)));
    }
}
