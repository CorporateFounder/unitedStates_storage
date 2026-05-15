package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35PowValidatorTest {
    private static final String LOW_HASH =
            "0000000000000000000000000000000000000000000000000000000000000001";
    private static final String HIGH_HASH =
            "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
    private static final BigInteger HIGH_TARGET = new BigInteger(HIGH_HASH, 16);

    @Test
    public void comparesHashNumberToTargetSafely() {
        assertTrue(V35PowValidator.isHashUnderTarget(LOW_HASH, HIGH_TARGET));
        assertFalse(V35PowValidator.isHashUnderTarget(HIGH_HASH, BigInteger.ONE));
        assertTrue(V35PowValidator.isHashUnderTarget("ff", new BigInteger("ff", 16)));
        assertFalse(V35PowValidator.isHashUnderTarget("not-a-hash", HIGH_TARGET));
        assertFalse(V35PowValidator.isHashUnderTarget(null, HIGH_TARGET));
        assertFalse(V35PowValidator.isHashUnderTarget("", HIGH_TARGET));
        assertFalse(V35PowValidator.isHashUnderTarget(LOW_HASH, null));
        assertFalse(V35PowValidator.isHashUnderTarget(LOW_HASH, BigInteger.ZERO));
        assertFalse(V35PowValidator.isHashUnderTarget(LOW_HASH, BigInteger.valueOf(-1)));
    }
}
