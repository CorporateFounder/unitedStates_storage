package International_Trade_Union.utils;

import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsResolvingResolveSyncGuardTest {

    @BeforeEach
    void resetResolveSyncDebug() {
        UtilsResolving.resetResolveSyncDebugForTests();
    }

    @Test
    void scenarioASelfPublicPeerIsSkippedBeforeResolveDownload() {
        assertTrue(UtilsResolving.shouldSkipResolvePeerForTests("http://194.87.236.238:82", 507098, 507100));

        Map<String, Object> status = UtilsResolving.resolveSyncDebugSnapshot();
        assertEquals(UtilsResolving.RESOLVE_SKIPPED_SELF_PEER, status.get("lastResolveSkippedReason"));
        assertEquals(1L, status.get("resolveSelfPeerSkippedCount"));
    }

    @Test
    void scenarioBLocalhostSelfPeersAreSkippedBeforeResolveDownload() {
        assertTrue(UtilsResolving.shouldSkipResolvePeerForTests("http://localhost:82", 507098, 507100));
        assertTrue(UtilsResolving.isSelfPeer("http://127.0.0.1:82"));
        assertTrue(UtilsResolving.isSelfPeer("localhost:82"));

        Map<String, Object> status = UtilsResolving.resolveSyncDebugSnapshot();
        assertEquals(UtilsResolving.RESOLVE_SKIPPED_SELF_PEER, status.get("lastResolveSkippedReason"));
    }

    @Test
    void scenarioCStalePeerBehindLocalNodeIsSkipped() {
        assertTrue(UtilsResolving.shouldSkipResolvePeerForTests("http://65.21.246.188:82", 507098, 482360));

        Map<String, Object> status = UtilsResolving.resolveSyncDebugSnapshot();
        assertEquals(UtilsResolving.RESOLVE_SKIPPED_STALE_PEER, status.get("lastResolveSkippedReason"));
        assertEquals(1L, status.get("resolveStalePeerSkippedCount"));
    }

    @Test
    void scenarioDPeerAheadWithEmptyDownloadListIsSkipped() {
        DataShortBlockchainInformation invalidTemp = invalidRollbackTemp(507098);

        assertTrue(UtilsResolving.shouldSkipInvalidRollbackForTests(
                new ArrayList<>(), new ArrayList<>(), invalidTemp, "http://203.0.113.10:82", 507098, 507100));

        Map<String, Object> status = UtilsResolving.resolveSyncDebugSnapshot();
        assertEquals(UtilsResolving.RESOLVE_SKIPPED_EMPTY_DOWNLOAD, status.get("lastResolveSkippedReason"));
        assertEquals(1L, status.get("resolveEmptyDownloadSkippedCount"));
    }

    @Test
    void scenarioEPeerAheadWithInvalidRollbackTempAndNoForkPointIsSkipped() {
        DataShortBlockchainInformation invalidTemp = invalidRollbackTemp(507098);
        List<Block> downloaded = List.of(block(507098), block(507099), block(507100));

        assertTrue(UtilsResolving.shouldSkipInvalidRollbackForTests(
                new ArrayList<>(), downloaded, invalidTemp, "http://203.0.113.10:82", 507098, 507100));

        Map<String, Object> status = UtilsResolving.resolveSyncDebugSnapshot();
        assertEquals(UtilsResolving.RESOLVE_SKIPPED_INVALID_ROLLBACK_TEMP, status.get("lastResolveSkippedReason"));
        assertEquals(3L, status.get("lastResolveDownloadedCount"));
        assertEquals(false, status.get("lastResolveRollbackTempValid"));
        assertEquals(1L, status.get("resolveInvalidRollbackSkippedCount"));
    }

    @Test
    void scenarioFBoundaryDownloadWithZeroWaitingDoesNotReadEmptyDifferentList() {
        DataShortBlockchainInformation invalidTemp = invalidRollbackTemp(507098);
        List<Block> downloaded = List.of(block(507098));

        assertThrows(IndexOutOfBoundsException.class, () -> new ArrayList<Block>().get(0));
        assertTrue(UtilsResolving.shouldSkipInvalidRollbackForTests(
                new ArrayList<>(), downloaded, invalidTemp, "http://203.0.113.10:82", 507098, 507098));

        Map<String, Object> status = UtilsResolving.resolveSyncDebugSnapshot();
        assertEquals(UtilsResolving.RESOLVE_SKIPPED_INVALID_ROLLBACK_TEMP, status.get("lastResolveSkippedReason"));
    }

    @Test
    void scenarioGTimeoutPeerCanBeRecordedWithoutRetainingResolveState() {
        UtilsResolving.shouldSkipInvalidRollbackForTests(
                new ArrayList<>(), new ArrayList<>(), invalidRollbackTemp(507098),
                "http://203.0.113.20:82", 507098, 507100);

        Map<String, Object> status = UtilsResolving.resolveSyncDebugSnapshot();
        assertEquals("http://203.0.113.20:82", status.get("lastResolvePeer"));
        assertEquals(UtilsResolving.RESOLVE_SKIPPED_EMPTY_DOWNLOAD, status.get("lastResolveSkippedReason"));
    }

    @Test
    void scenarioHMixedPeersFinishWithSkipReasonsAndNoBoundsException() {
        assertTrue(UtilsResolving.shouldSkipResolvePeerForTests("http://194.87.236.238:82", 507098, 507100));
        assertTrue(UtilsResolving.shouldSkipResolvePeerForTests("http://65.21.246.188:82", 507098, 482360));
        assertTrue(UtilsResolving.shouldSkipInvalidRollbackForTests(
                new ArrayList<>(), new ArrayList<>(), invalidRollbackTemp(507098),
                "http://203.0.113.10:82", 507098, 507100));
        assertFalse(UtilsResolving.shouldSkipResolvePeerForTests("http://203.0.113.30:82", 507098, 507099));

        Map<String, Object> status = UtilsResolving.resolveSyncDebugSnapshot();
        assertEquals(1L, status.get("resolveSelfPeerSkippedCount"));
        assertEquals(1L, status.get("resolveStalePeerSkippedCount"));
        assertEquals(1L, status.get("resolveEmptyDownloadSkippedCount"));
    }

    @Test
    void exactRegressionDifferentEmptyDownloadedAheadWouldHaveReadIndexZero() {
        List<Block> different = new ArrayList<>();
        List<Block> downloadedAhead = List.of(block(507098), block(507099), block(507100));

        assertThrows(IndexOutOfBoundsException.class, () -> different.get(0));
        assertTrue(UtilsResolving.shouldSkipInvalidRollbackForTests(
                different, downloadedAhead, invalidRollbackTemp(507098),
                "http://203.0.113.10:82", 507098, 507100));
    }

    @Test
    void constantsRemainActivationSafe() {
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    private static DataShortBlockchainInformation invalidRollbackTemp(long size) {
        DataShortBlockchainInformation temp = new DataShortBlockchainInformation();
        temp.setSize(size);
        temp.setValidation(false);
        temp.setHashCount(0);
        temp.setTransactions(0);
        return temp;
    }

    private static Block block(long index) {
        Block block = new Block();
        block.setIndex(index);
        block.setHashBlock("hash-" + index);
        return block;
    }
}
