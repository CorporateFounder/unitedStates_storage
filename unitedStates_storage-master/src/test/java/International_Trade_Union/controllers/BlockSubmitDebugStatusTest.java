package International_Trade_Union.controllers;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.LiteVersionWiner;
import International_Trade_Union.model.PeerConnectionDiagnostics;
import International_Trade_Union.model.PeerHealthRegistry;
import International_Trade_Union.model.PeerNetworkPolicy;
import International_Trade_Union.model.TournamentSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockSubmitDebugStatusTest {

    @Test
    void debugStatusStartsReadOnlyAndIncludesMemoryWithoutLoadingBlocks() {
        BasisController.resetBlockSubmitDebugForTests();

        Map<String, Object> status = BasisController.blockSubmitStatusSnapshotForTests();

        assertEquals(-1L, status.get("lastReceivedBlockIndex"));
        assertEquals("none", status.get("lastReceivedBlockHash"));
        assertEquals("none", status.get("lastRejectReason"));
        assertTrue(status.containsKey("currentSize"));
        assertTrue(status.containsKey("temporaryBlockQueueSize"));
        assertTrue(status.containsKey("freeMemory"));
        assertTrue(status.containsKey("totalMemory"));
        assertTrue(status.containsKey("maxMemory"));
        assertTrue(status.containsKey("lastPeerPropagationStartedAt"));
        assertTrue(status.containsKey("lastPeerPropagationSkippedReason"));
        assertTrue(status.containsKey("lastMemoryGuardDecision"));
        assertTrue(status.containsKey("lastPersistedBlockIndex"));
        assertTrue(status.containsKey("lastPostPersistAction"));
        assertEquals(false, status.get("prePersistPeerPropagationEnabled"));
        assertTrue(status.containsKey("lastPrePersistPeerPropagationSkippedReason"));
        assertTrue(status.containsKey("lastPrePersistPeerPropagationAttempted"));
        assertTrue(status.containsKey("lastPrePersistPeerPropagationSkippedAt"));
        assertTrue(status.containsKey("lastResolveConflictReceivedBlockIndex"));
        assertTrue(status.containsKey("lastResolveConflictQueuedBlockIndex"));
        assertTrue(status.containsKey("lastResolveConflictBeforeTournamentMemoryUsed"));
        assertTrue(status.containsKey("lastResolveConflictBeforeTournamentMemoryMax"));
        assertTrue(status.containsKey("lastLocalPersistenceStartedAt"));
        assertTrue(status.containsKey("lastLocalPersistenceFinishedAt"));
        assertTrue(status.containsKey("lastLocalPersistenceResult"));
        assertTrue(status.containsKey("selfPeerSkippedCount"));
        assertTrue(status.containsKey("prePersistOutboundTasksBlockedCount"));
        assertTrue(status.containsKey("lastResolveSkippedReason"));
        assertTrue(status.containsKey("lastResolvePeer"));
        assertTrue(status.containsKey("lastResolveLocalSize"));
        assertTrue(status.containsKey("lastResolvePeerSize"));
        assertTrue(status.containsKey("lastResolveRequestedDownloadCount"));
        assertTrue(status.containsKey("lastResolveDownloadedCount"));
        assertTrue(status.containsKey("lastResolveRollbackTempValid"));
        assertTrue(status.containsKey("resolveSelfPeerSkippedCount"));
        assertTrue(status.containsKey("resolveStalePeerSkippedCount"));
        assertTrue(status.containsKey("resolveEmptyDownloadSkippedCount"));
        assertTrue(status.containsKey("resolveInvalidRollbackSkippedCount"));
        assertTrue(status.containsKey("resolveBoundsGuardCount"));
        assertTrue(status.containsKey("lastResolveException"));
        assertTrue(status.containsKey("lastSubmitTraceId"));
        assertTrue(status.containsKey("lastTournamentTraceId"));
        assertTrue(status.containsKey("lastAddBlock3TraceId"));
        assertTrue(status.containsKey("lastQueuedBlockIndex"));
        assertTrue(status.containsKey("lastQueueSizeAfterAdd"));
        assertTrue(status.containsKey("lastTournamentCandidateCount"));
        assertTrue(status.containsKey("lastTournamentWinnerIndex"));
        assertTrue(status.containsKey("lastAddBlock3StartedAt"));
        assertTrue(status.containsKey("lastAddBlock3FinishedAt"));
        assertTrue(status.containsKey("lastAddBlock3Result"));
        assertTrue(status.containsKey("lastSizeBeforeAdd"));
        assertTrue(status.containsKey("lastSizeAfterAdd"));
        assertTrue(status.containsKey("lastSizeDelta"));
        assertTrue(status.containsKey("lastSubmitError"));
        assertTrue(status.containsKey("lastNoGrowthReason"));
    }

    @Test
    void receivedBlockAndRejectionReasonAreVisible() {
        BasisController.resetBlockSubmitDebugForTests();

        BasisController.recordReceivedBlockForTests(507174L, "abc123", "127.0.0.1");
        BasisController.recordValidationResultForTests("rejected", "previousHashMismatch=true");

        Map<String, Object> status = BasisController.blockSubmitStatusSnapshotForTests();

        assertEquals(507174L, status.get("lastReceivedBlockIndex"));
        assertEquals("abc123", status.get("lastReceivedBlockHash"));
        assertEquals("127.0.0.1", status.get("lastSubmitRemoteAddress"));
        assertEquals("rejected", status.get("lastValidationResult"));
        assertEquals("previousHashMismatch=true", status.get("lastRejectReason"));
    }

    @Test
    void addedBlockStateIsVisibleAndConstantsRemainPrepared() {
        BasisController.resetBlockSubmitDebugForTests();

        BasisController.recordAddedBlockForTests(507174L, "def456");

        Map<String, Object> status = BasisController.blockSubmitStatusSnapshotForTests();

        assertEquals(507174L, status.get("lastAddedBlockIndex"));
        assertEquals("def456", status.get("lastAddedBlockHash"));
        assertFalse(String.valueOf(status.get("lastAddedAt")).isBlank());
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    @Test
    void prePersistPeerPropagationIsDisabledByDefaultForProductionSafety() {
        BasisController.resetBlockSubmitDebugForTests();

        Map<String, Object> status = BasisController.blockSubmitStatusSnapshotForTests();

        assertFalse(BasisController.isPrePersistPeerPropagationEnabled());
        assertEquals(false, status.get("prePersistPeerPropagationEnabled"));
        assertEquals(false, status.get("lastPrePersistPeerPropagationAttempted"));
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    @Test
    void persistedBlockStateIsVisibleForPostPersistDiagnostics() {
        BasisController.resetBlockSubmitDebugForTests();
        Block block = new Block();
        block.setIndex(507336L);
        block.setHashBlock("persisted-hash");

        BasisController.recordPersistedBlockFromTournament(block);

        Map<String, Object> status = BasisController.blockSubmitStatusSnapshotForTests();

        assertEquals(507336L, status.get("lastPersistedBlockIndex"));
        assertEquals("persisted-hash", status.get("lastPersistedBlockHash"));
        assertEquals("persisted", status.get("lastValidationResult"));
    }

    @Test
    void memoryStatusSnapshotIncludesRuntimeAndTournamentCounters() {
        Map<String, Object> status = BasisController.memoryStatusSnapshotForTests();

        assertTrue(status.containsKey("currentChainSize"));
        assertTrue(status.containsKey("freeMemory"));
        assertTrue(status.containsKey("totalMemory"));
        assertTrue(status.containsKey("maxMemory"));
        assertTrue(status.containsKey("usedMemory"));
        assertTrue(status.containsKey("winnerListSize"));
        assertTrue(status.containsKey("temporaryBlocksSize"));
        assertTrue(status.containsKey("pendingSubmittedBlocksSize"));
        assertTrue(status.containsKey("temporarySubmittedBlocksCap"));
        assertTrue(status.containsKey("poolBlockMapSize"));
        assertTrue(status.containsKey("pendingTxMapSize"));
        assertTrue(status.containsKey("activePeerCount"));
        assertTrue(status.containsKey("unreachablePeerCount"));
        assertTrue(status.containsKey("lastTournamentStartTime"));
        assertTrue(status.containsKey("lastTournamentEndTime"));
        assertTrue(status.containsKey("lastTournamentDuration"));
        assertTrue(status.containsKey("lastResolve3Duration"));
        assertTrue(status.containsKey("lastOomRiskWarning"));
        assertTrue(status.containsKey("lastPeerPropagationPeerCount"));
        assertTrue(status.containsKey("lastPeerPropagationSuccessCount"));
        assertTrue(status.containsKey("lastPeerPropagationFailureCount"));
        assertTrue(status.containsKey("lastMemoryGuardFreeMemory"));
        assertTrue(status.containsKey("lastMemoryGuardMaxMemory"));
        assertEquals(100, status.get("temporarySubmittedBlocksCap"));
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    @Test
    void liveStateSnapshotContainsCountsAndNoFullBlockOrTransactionPayload() {
        BasisController.resetBlockSubmitDebugForTests();
        CopyOnWriteArrayList<Block> blocks = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 120; i++) {
            blocks.add(testBlock(i));
        }
        BasisController.setWinnerList(blocks);

        Map<String, Object> status = BasisController.memoryLiveStateSnapshotForTests();
        String serialized = String.valueOf(status);

        assertEquals(100, status.get("winnerListSize"));
        assertEquals(100, status.get("currentCandidatesSize"));
        assertTrue(status.containsKey("activeRequestsByEndpoint"));
        assertTrue(status.containsKey("activeFuturesByPhase"));
        assertTrue(status.containsKey("currentThreadCount"));
        assertFalse(serialized.contains("dtoTransactions"));
        assertFalse(serialized.contains("previousHash='"));
        assertFalse(serialized.contains("minerAddress='"));
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    @Test
    void publicTournamentSnapshotsStayBoundedAndStoreLiteDtosOnly() {
        BasisController.resetTournamentSnapshotsForTests();
        List<LiteVersionWiner> candidates = new ArrayList<>();
        for (int i = 0; i < 120; i++) {
            LiteVersionWiner candidate = new LiteVersionWiner(i, "miner-" + i, "hash-" + i, i, i, i, i);
            candidate.setPreviousHash("previous-" + i);
            candidate.setTimestamp("2026-06-17T00:00:00Z");
            candidates.add(candidate);
        }

        BasisController.publishTournamentSnapshot(candidates, 1L, 2L, "test-stage", "test-reason");
        TournamentSnapshot snapshot = BasisController.getLastTournamentSnapshot();

        assertEquals(100, snapshot.getCandidates().size());
        assertEquals(100, BasisController.lastTournamentCandidatesSnapshot().size());
        assertEquals(LiteVersionWiner.class, snapshot.getCandidates().get(0).getClass());
        assertEquals("hash-0", snapshot.getCandidates().get(0).getHashBlock());
        assertEquals("miner-0", snapshot.getCandidates().get(0).getMinerAddress());
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    @Test
    void selfPeerAndConnectionDiagnosticsExposeCountsOnly() {
        PeerConnectionDiagnostics.resetForTests();
        PeerHealthRegistry.resetForTests();

        PeerConnectionDiagnostics.beginRequest("/winnerList");
        PeerConnectionDiagnostics.beginFutures("tournament_getTransactions", 3);
        PeerHealthRegistry.recordSelfSkipped("http://127.0.0.1:82", "test");

        Map<String, Object> connection = PeerHealthRegistry.connectionSnapshot();
        Map<String, Object> self = PeerHealthRegistry.selfPeerSnapshot();

        assertTrue(PeerNetworkPolicy.isKnownSelfPeer("http://0.0.0.0:82"));
        assertTrue(PeerNetworkPolicy.isKnownSelfPeer("http://127.0.0.1:82"));
        assertTrue(PeerNetworkPolicy.isKnownSelfPeer("http://localhost:82"));
        assertTrue(PeerNetworkPolicy.isKnownSelfPeer("http://194.87.236.238:82"));
        assertEquals(1, connection.get("activePeerTasksCount"));
        assertEquals(3, connection.get("activeTournamentFuturesCount"));
        assertTrue(((Number) self.get("skippedSelfCount")).intValue() >= 1);
        assertEquals(true, self.get("publicHostTreatedAsSelf"));

        PeerConnectionDiagnostics.endRequest("/winnerList");
        PeerConnectionDiagnostics.endFutures("tournament_getTransactions", 3);
    }

    @Test
    void diagnosticRoutesExposeSubmitTournamentAddBlockAndRuntimeTraces() throws Exception {
        assertRoute("blockSubmitTraces", "/debug/block-submit/traces");
        assertRoute("tournamentTraces", "/debug/tournament/traces");
        assertRoute("addBlockTraces", "/debug/add-block/traces");
        assertRoute("nodeRuntime", "/debug/node/runtime");
        assertRoute("currentCandidates", "/currentCandidates");
        assertRoute("lastTournament", "/lastTournament");
        assertRoute("lastTournamentWinner", "/lastTournamentWinner");
        assertRoute("memoryLiveState", "/debug/memory/live-state");
        assertRoute("selfPeerDiagnostics", "/debug/peers/self");
        assertRoute("peerConnectionDiagnostics", "/debug/peer/connections");
    }

    private static Block testBlock(long index) {
        Block block = new Block();
        block.setIndex(index);
        block.setHashBlock("block-hash-" + index);
        block.setPreviousHash("previous-hash-" + index);
        block.setMinerAddress("miner-" + index);
        return block;
    }

    private static void assertRoute(String methodName, String expectedPath) throws NoSuchMethodException {
        Method method = BasisController.class.getDeclaredMethod(methodName);
        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertTrue(mapping != null, methodName + " must have @GetMapping");
        assertEquals(expectedPath, mapping.value()[0]);
    }
}
