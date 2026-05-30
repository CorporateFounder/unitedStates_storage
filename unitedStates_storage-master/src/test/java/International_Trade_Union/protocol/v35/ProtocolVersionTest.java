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
        assertFalse(ProtocolVersion.isV35(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex()));
        assertTrue(ProtocolVersion.isLegacy(0));
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
    }
}
