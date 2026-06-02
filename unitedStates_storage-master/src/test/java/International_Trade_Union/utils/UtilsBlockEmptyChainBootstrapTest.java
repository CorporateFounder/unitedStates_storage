package International_Trade_Union.utils;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.protocol.v35.ProtocolVersion;
import International_Trade_Union.protocol.v35.V35TestMode;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    public void v35RemainsInactiveBeforeForkDuringBootstrapHardening() {
        V35TestMode.assertConfiguredForkSafety();
        assertFalse(ProtocolVersion.isV35(0L));
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }
}
