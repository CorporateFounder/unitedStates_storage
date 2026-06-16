package International_Trade_Union.utils;

import International_Trade_Union.model.PeerNetworkPolicy;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsAllAddressesMemoryGuardTest {

    @AfterEach
    void cleanup() {
        System.clearProperty("citu.peer.maxBlockedHostEntries");
    }

    @Test
    void blockedHostPathIsRecognizedForReadCap() {
        assertTrue(UtilsAllAddresses.isBlockedHostPath(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE));
        assertFalse(UtilsAllAddresses.isBlockedHostPath(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE));
    }

    @Test
    void blockedHostCapDefaultsToThirtyTwoAndCanBeLowered() {
        assertEquals(32, PeerNetworkPolicy.maxBlockedHostEntries());

        System.setProperty("citu.peer.maxBlockedHostEntries", "7");

        assertEquals(7, PeerNetworkPolicy.maxBlockedHostEntries());
    }
}
