package International_Trade_Union.utils;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsResolvingMemoryGuardTest {

    @Test
    void propagationHostCapTracksConfiguredRandomHosts() {
        assertEquals(Math.max(1, Seting.RANDOM_HOSTS), UtilsResolving.MAX_PROPAGATION_HOSTS);
    }

    @Test
    void peerPoolSizeIsNeverZeroOrAbovePropagationCap() {
        assertEquals(1, UtilsResolving.boundedPeerPoolSize(0));
        assertEquals(1, UtilsResolving.boundedPeerPoolSize(1));

        int oversizedPool = UtilsResolving.boundedPeerPoolSize(UtilsResolving.MAX_PROPAGATION_HOSTS * 10);

        assertTrue(oversizedPool >= 1);
        assertTrue(oversizedPool <= UtilsResolving.MAX_PROPAGATION_HOSTS);
        assertTrue(oversizedPool <= Runtime.getRuntime().availableProcessors());
    }
}
