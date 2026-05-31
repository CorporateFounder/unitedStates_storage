package International_Trade_Union.utils;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsBlockEmptyChainBootstrapTest {
    @Test
    public void emptyChainUsesLegacyBootstrapDifficulty() {
        assertEquals(1L, UtilsBlock.difficulty(
                Collections.emptyList(),
                Seting.BLOCK_GENERATION_INTERVAL,
                Seting.DIFFICULTY_ADJUSTMENT_INTERVAL));
    }

    @Test
    public void nullChainUsesLegacyBootstrapDifficulty() {
        assertEquals(1L, UtilsBlock.difficulty(
                null,
                Seting.BLOCK_GENERATION_INTERVAL,
                Seting.DIFFICULTY_ADJUSTMENT_INTERVAL));
    }

    @Test
    public void v35RemainsDisabledDuringBootstrapHardening() {
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }
}
