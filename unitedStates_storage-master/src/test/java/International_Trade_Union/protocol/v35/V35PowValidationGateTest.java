package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35PowValidationGateTest {
    @Test
    public void compactTargetDecodeAndHashValidationAcceptLowHash() {
        BigInteger target = new BigInteger("1234560000", 16);
        long compact = V35TargetDifficulty.encodeCompactTarget(target);
        BigInteger decodedTarget = V35TargetDifficulty.decodeCompactTarget(compact);

        assertEquals(target, decodedTarget);
        assertTrue(V35PowValidator.isHashUnderTarget("0000000001", decodedTarget));
    }

    @Test
    public void compactTargetDecodeAndHashValidationRejectHighHash() {
        BigInteger target = BigInteger.ONE;
        long compact = V35TargetDifficulty.encodeCompactTarget(target);
        BigInteger decodedTarget = V35TargetDifficulty.decodeCompactTarget(compact);

        assertEquals(target, decodedTarget);
        assertFalse(V35PowValidator.isHashUnderTarget("ff", decodedTarget));
    }

    @Test
    public void invalidCompactTargetOrHashIsRejected() {
        BigInteger invalidTarget = V35TargetDifficulty.decodeCompactTarget(0L);

        assertEquals(BigInteger.ZERO, invalidTarget);
        assertFalse(V35PowValidator.isHashUnderTarget("01", invalidTarget));
        assertFalse(V35PowValidator.isHashUnderTarget("not-a-hash", BigInteger.TEN));
    }

    @Test
    public void protocolVersionKeepsV35DisabledForNormalIndexes() {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertFalse(ProtocolVersion.isV35(0));
        assertFalse(ProtocolVersion.isV35(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex()));
        assertTrue(ProtocolVersion.isLegacy(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex()));
    }
}
