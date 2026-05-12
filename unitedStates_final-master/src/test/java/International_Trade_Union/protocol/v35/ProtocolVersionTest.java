package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProtocolVersionTest {
    @Test
    public void v35RemainsDisabledForNormalIndexes() {
        assertFalse(ProtocolVersion.isV35(0));
        assertFalse(ProtocolVersion.isV35(1_000_000L));
        assertTrue(ProtocolVersion.isLegacy(0));
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
    }
}
