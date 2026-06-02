package International_Trade_Union.model;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.protocol.v35.ProtocolVersion;
import International_Trade_Union.protocol.v35.V35TestMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TournamentOfflineSmokeModeTest {
    @Test
    public void offlineSmokeModeDisablesTournamentNetworkServices() {
        assertFalse(Tournament.shouldStartNetworkServices(true));
    }

    @Test
    public void normalModeStillStartsTournamentNetworkServices() {
        assertTrue(Tournament.shouldStartNetworkServices(false));
    }

    @Test
    public void v35RemainsInactiveBeforeForkDuringOfflineSmokeHardening() {
        V35TestMode.assertConfiguredForkSafety();
        assertFalse(ProtocolVersion.isV35(0L));
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }
}
