package International_Trade_Union.model;

import International_Trade_Union.setings.Seting;
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
    public void v35RemainsDisabledDuringOfflineSmokeHardening() {
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }
}
