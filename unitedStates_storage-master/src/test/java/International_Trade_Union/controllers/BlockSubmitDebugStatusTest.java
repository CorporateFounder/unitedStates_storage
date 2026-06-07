package International_Trade_Union.controllers;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
}
