package International_Trade_Union.utils;

import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsResolvingPeerPropagationSafetyTest {

    @Test
    void selfPeersAreSkippedForPublicAndLocalNodeUrls() {
        assertTrue(UtilsResolving.isSelfPeer("http://194.87.236.238:82"));
        assertTrue(UtilsResolving.isSelfPeer("http://194.87.236.238:82/"));
        assertTrue(UtilsResolving.isSelfPeer("http://127.0.0.1:82"));
        assertTrue(UtilsResolving.isSelfPeer("https://127.0.0.1:82/"));
        assertTrue(UtilsResolving.isSelfPeer("localhost:82"));
        assertFalse(UtilsResolving.isSelfPeer("http://203.0.113.10:82"));
    }

    @Test
    void lowMemoryGuardSkipsBeforeBuildingPeerPayloads() {
        long maxMemory = 1024L * 1024L * 1024L;
        long totalMemory = 950L * 1024L * 1024L;
        long lowFreeMemory = 16L * 1024L * 1024L;

        assertTrue(UtilsResolving.shouldSkipPeerPropagationForMemory(lowFreeMemory, totalMemory, maxMemory));
        assertFalse(UtilsResolving.shouldSkipPeerPropagationForMemory(512L * 1024L * 1024L, totalMemory, maxMemory));
    }

    @Test
    void singleBlockPropagationPayloadDoesNotSendFullBlockchainList() {
        Block oldBlock = new Block();
        oldBlock.setIndex(1);
        oldBlock.setHashBlock("old");
        Block latestBlock = new Block();
        latestBlock.setIndex(2);
        latestBlock.setHashBlock("latest");

        List<Block> payload = UtilsResolving.minimalPropagationPayload(List.of(oldBlock, latestBlock));

        assertEquals(1, payload.size());
        assertSame(latestBlock, payload.get(0));
    }

    @Test
    void propagationPeerCountIsCappedAndConstantsRemainActivationSafe() {
        assertTrue(UtilsResolving.MAX_PROPAGATION_HOSTS <= 3);
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }
}
