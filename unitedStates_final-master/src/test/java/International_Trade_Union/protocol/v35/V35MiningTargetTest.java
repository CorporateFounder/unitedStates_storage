package International_Trade_Union.protocol.v35;

import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35MiningTargetTest {
    @Test
    public void miningTargetHelperReturnsPositiveCompactTarget() {
        long compactTarget = Mining.selectV35MiningCompactTarget(Collections.emptyList());
        BigInteger target = V35TargetDifficulty.decodeCompactTarget(compactTarget);

        assertEquals(V35TargetDifficulty.getInitialCompactTarget(), compactTarget);
        assertTrue(target.signum() > 0);
        assertTrue(V35PowValidator.isHashUnderTarget(target.toString(16), target));
    }

    @Test
    public void v35RemainsDisabledAndLegacyCustomDiffStillWorks() {
        int originalCustomDiff = Mining.getCustomDiff();
        try {
            int customDiff = Seting.V34_MIN_DIFF + 7;
            Mining.setCustomDiff(customDiff);

            assertEquals(customDiff, Mining.getCustomDiff());
            assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
            assertFalse(ProtocolVersion.isV35(1_000_000L));
        } finally {
            Mining.setCustomDiff(originalCustomDiff);
        }
    }
}
