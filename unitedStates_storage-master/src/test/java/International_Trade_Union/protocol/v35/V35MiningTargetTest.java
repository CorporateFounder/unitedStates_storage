package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public void miningTargetUsesRollingWindowWhenEnoughV35HistoryExists() {
        long compactTarget = V35TargetDifficulty.getInitialCompactTarget();
        long expected = (long) V35TargetDifficulty.getTargetWindow() * V35TargetDifficulty.getTargetBlockTimeSeconds();
        List<Block> chain = v35Blocks(compactTarget, expected);

        long selected = Mining.selectV35MiningCompactTarget(chain);

        assertEquals(compactTarget, selected);
        assertTrue(V35TargetDifficulty.decodeCompactTarget(selected).signum() > 0);
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

    private static List<Block> v35Blocks(long compactTarget, long actualTimespanSeconds) {
        List<Block> blocks = new ArrayList<>();
        int window = V35TargetDifficulty.getTargetWindow();
        long now = System.currentTimeMillis();
        long start = now - (actualTimespanSeconds * 1000L);
        for (int i = 0; i < window; i++) {
            long timestamp = start + ((actualTimespanSeconds * 1000L * i) / (window - 1));
            blocks.add(new Block(Collections.emptyList(), "previous", "miner", "founder",
                    i, 0.0, compactTarget, new Timestamp(timestamp), Long.MAX_VALUE, ""));
        }
        return blocks;
    }
}
