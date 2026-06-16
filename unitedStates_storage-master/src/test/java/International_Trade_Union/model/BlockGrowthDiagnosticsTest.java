package International_Trade_Union.model;

import International_Trade_Union.entity.blockchain.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlockGrowthDiagnosticsTest {
    @AfterEach
    void tearDown() {
        System.clearProperty(BlockGrowthDiagnostics.DIAGNOSTIC_TRACES_PROPERTY);
        BlockGrowthDiagnostics.clearForTests();
    }

    @Test
    void diagnosticTracesAreDisabledByDefaultButStatusStillUpdates() {
        Block block = block(1);

        String traceId = BlockGrowthDiagnostics.startSubmit("/nodes/resolve_from_to_block", "127.0.0.1", 512, block, 0, 0);
        BlockGrowthDiagnostics.submitValidationFinished(traceId, "valid", "none");
        BlockGrowthDiagnostics.submitQueued(traceId, block, 1);

        assertFalse(BlockGrowthDiagnostics.tracesEnabled());
        assertEquals("disabled", traceId);
        assertTrue(BlockGrowthDiagnostics.submitTraces().isEmpty());
        assertEquals(1L, BlockGrowthDiagnostics.submitStatus().get("lastReceivedBlockIndex"));
        assertEquals("valid", BlockGrowthDiagnostics.submitStatus().get("lastValidationResult"));
        assertEquals(1, BlockGrowthDiagnostics.submitStatus().get("lastQueueSizeAfterAdd"));
        assertEquals(false, BlockGrowthDiagnostics.memoryForensicsFields().get("diagnosticTracesEnabled"));
    }

    @Test
    void keepsOnlyLastThreeSubmitTournamentAndAddBlockTraces() {
        enableTraces();
        for (int i = 0; i < 5; i++) {
            Block block = block(i);
            String submitTrace = BlockGrowthDiagnostics.startSubmit("/nodes/resolve_from_to_block", "127.0.0.1", 512, block, i, i);
            BlockGrowthDiagnostics.submitValidationStarted(submitTrace);
            BlockGrowthDiagnostics.submitValidationFinished(submitTrace, "valid", "none");
            BlockGrowthDiagnostics.submitQueued(submitTrace, block, i + 1);

            String tournamentTrace = BlockGrowthDiagnostics.startTournament(i, i + 1);
            BlockGrowthDiagnostics.tournamentCandidates(tournamentTrace, i + 1);
            BlockGrowthDiagnostics.tournamentWinnerSelected(tournamentTrace, block, 1);
            BlockGrowthDiagnostics.tournamentAddBlock3Result(tournamentTrace, true, i, i + 1);
            BlockGrowthDiagnostics.finishTournament(tournamentTrace, i + 1, 0);

            String addTrace = BlockGrowthDiagnostics.startAddBlock3(List.of(block), i);
            BlockGrowthDiagnostics.addBlock3Stage(addTrace, "database save finished", Map.of("accountSaveCount", 2));
            BlockGrowthDiagnostics.finishAddBlock3(addTrace, true, i + 1, block, null);
        }

        assertEquals(3, BlockGrowthDiagnostics.submitTraces().size());
        assertEquals(3, BlockGrowthDiagnostics.tournamentTraces().size());
        assertEquals(3, BlockGrowthDiagnostics.addBlock3Traces().size());
        assertEquals(4L, BlockGrowthDiagnostics.submitStatus().get("lastReceivedBlockIndex"));
        assertEquals("BLOCK_GROWTH_CONFIRMED", BlockGrowthDiagnostics.submitStatus().get("lastNoGrowthReason"));
        assertTrue(BlockGrowthDiagnostics.submitTraces().get(2).containsKey("receivedAt"));
        assertEquals("QUEUED_NOT_CONSUMED", BlockGrowthDiagnostics.tournamentTraces().get(2).get("noGrowthReason"));
        assertEquals(true, BlockGrowthDiagnostics.tournamentTraces().get(2).get("winnerSelected"));
        assertEquals(4L, BlockGrowthDiagnostics.tournamentTraces().get(2).get("winnerIndex"));
        assertEquals("database save finished", BlockGrowthDiagnostics.addBlock3Traces().get(2).get("lastStage"));
    }

    @Test
    void submitTraceStoresCompactMetadataOnly() {
        enableTraces();
        Block block = block(514291);
        block.setHashBlock("1234567890abcdefFULL_HASH_SHOULD_NOT_BE_STORED");
        block.setPreviousHash("abcdef1234567890FULL_PREVIOUS_HASH_SHOULD_NOT_BE_STORED");
        block.setMinerAddress("miner-address-with-private-looking-long-value");

        BlockGrowthDiagnostics.startSubmit("/nodes/resolve_from_to_block", "127.0.0.1", 1024, block, 514291, 0);

        Map<String, Object> trace = BlockGrowthDiagnostics.submitTraces().get(0);
        assertEquals("1234567890abcdef", trace.get("candidateHashPrefix"));
        assertEquals("abcdef1234567890", trace.get("candidatePreviousHashPrefix"));
        assertEquals("miner-address-wi", trace.get("candidateMinerPrefix"));
        assertFalse(trace.toString().contains("FULL_HASH_SHOULD_NOT_BE_STORED"));
        assertFalse(trace.toString().contains("FULL_PREVIOUS_HASH_SHOULD_NOT_BE_STORED"));
        assertFalse(trace.containsKey("fullBlock"));
        assertFalse(trace.containsKey("transactions"));
    }

    @Test
    void noGrowthReasonsIdentifyRejectAndSizeUnchanged() {
        enableTraces();
        Block block = block(1);
        String submitTrace = BlockGrowthDiagnostics.startSubmit("/nodes/resolve_from_to_block", "127.0.0.1", -1, block, 1, 0);
        BlockGrowthDiagnostics.submitValidationFinished(submitTrace, "rejected", "bad previous hash");
        assertEquals("SUBMIT_RECEIVED_VALIDATION_FAILED", BlockGrowthDiagnostics.submitStatus().get("lastNoGrowthReason"));

        String addTrace = BlockGrowthDiagnostics.startAddBlock3(List.of(block), 1);
        BlockGrowthDiagnostics.finishAddBlock3(addTrace, true, 1, block, null);
        assertEquals("ADD_SUCCEEDED_SIZE_NOT_CHANGED", BlockGrowthDiagnostics.submitStatus().get("lastNoGrowthReason"));
    }

    @Test
    void localPersistenceTraceShowsSizeGrowthOrMissingGrowth() {
        enableTraces();
        Block block = block(10);
        String submitTrace = BlockGrowthDiagnostics.startSubmit("/nodes/resolve_from_to_block", "127.0.0.1", 128, block, 9, 0);
        BlockGrowthDiagnostics.submitQueued(submitTrace, block, 1);

        BlockGrowthDiagnostics.localPersistenceStarted(block);
        BlockGrowthDiagnostics.localPersistenceFinished(block, 10, "persisted");

        Map<String, Object> status = BlockGrowthDiagnostics.submitStatus();
        assertEquals(10L, status.get("lastSizeAfterAdd"));
        assertEquals(1L, status.get("lastSizeDelta"));
        assertEquals("BLOCK_GROWTH_CONFIRMED", status.get("lastNoGrowthReason"));

        Map<String, Object> trace = BlockGrowthDiagnostics.submitTraces().get(0);
        assertEquals("persisted", trace.get("localPersistenceResult"));
        assertEquals(10L, trace.get("addedBlockIndex"));
        assertEquals(1L, trace.get("sizeDelta"));
    }

    @Test
    void addBlockTraceMapsCriticalPersistenceStages() {
        enableTraces();
        Block block = block(11);
        String traceId = BlockGrowthDiagnostics.startAddBlock3(List.of(block), 10);

        BlockGrowthDiagnostics.addBlock3Stage(traceId, "balance calculation started", Map.of());
        BlockGrowthDiagnostics.addBlock3Stage(traceId, "balance calculation finished", Map.of());
        BlockGrowthDiagnostics.addBlock3Stage(traceId, "database save started", Map.of("accountSaveCount", 7));
        BlockGrowthDiagnostics.addBlock3Stage(traceId, "database save finished", Map.of());
        BlockGrowthDiagnostics.addBlock3Stage(traceId, "file save started", Map.of());
        BlockGrowthDiagnostics.addBlock3Stage(traceId, "file save finished", Map.of());
        BlockGrowthDiagnostics.finishAddBlock3(traceId, true, 11, block, null);

        Map<String, Object> trace = BlockGrowthDiagnostics.addBlock3Traces().get(0);
        assertTrue(trace.containsKey("calculateBalanceStartedAt"));
        assertTrue(trace.containsKey("calculateBalanceFinishedAt"));
        assertTrue(trace.containsKey("dbSaveStartedAt"));
        assertTrue(trace.containsKey("dbSaveFinishedAt"));
        assertTrue(trace.containsKey("fileSaveStartedAt"));
        assertTrue(trace.containsKey("fileSaveFinishedAt"));
        assertEquals(7, trace.get("accountSaveCount"));
        assertEquals(true, trace.get("result"));
    }

    private static Block block(long index) {
        Block block = new Block();
        block.setIndex(index);
        block.setTimestamp(new Timestamp(System.currentTimeMillis()));
        block.setHashBlock("hash-" + index + "-0123456789abcdef");
        block.setPreviousHash("prev-" + index + "-0123456789abcdef");
        block.setMinerAddress("miner-" + index + "-0123456789abcdef");
        block.setDtoTransactions(List.of());
        return block;
    }

    private static void enableTraces() {
        System.setProperty(BlockGrowthDiagnostics.DIAGNOSTIC_TRACES_PROPERTY, "true");
    }
}
