package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentSnapshotTest {
    @AfterEach
    void tearDown() {
        resetTournamentSnapshots();
        BasisController.setWinnerList(new CopyOnWriteArrayList<>());
        BasisController.setSizeWinnerList(0);
        BasisController.setAllWiners(new ArrayList<>());
    }

    @Test
    void currentCandidatesSnapshotReturnsAllCurrentCandidates() {
        CopyOnWriteArrayList<Block> candidates = new CopyOnWriteArrayList<>();
        candidates.add(block(514292L, "miner-a", "hash-a"));
        candidates.add(block(514292L, "miner-b", "hash-b"));
        candidates.add(block(514292L, "miner-c", "hash-c"));
        BasisController.setWinnerList(candidates);
        BasisController.setSizeWinnerList(candidates.size());

        List<Block> snapshot = BasisController.currentCandidatesSnapshot();

        assertEquals(3, snapshot.size());
        assertEquals("miner-a", snapshot.get(0).getMinerAddress());
        assertEquals("miner-c", snapshot.get(2).getMinerAddress());
    }

    @Test
    void tournamentSnapshotKeepsAllRankedCandidatesAndWinner() {
        List<LiteVersionWiner> ranked = List.of(
                lite(514292L, "winner-miner", "winner-hash", 9, 42, 100),
                lite(514292L, "second-miner", "second-hash", 7, 40, 90));

        BasisController.publishTournamentSnapshot(ranked, 1000L, 2000L,
                "tournament after sortWinner", "ranked_by_tournament_sort");

        TournamentSnapshot snapshot = BasisController.getLastTournamentSnapshot();

        assertNotNull(snapshot);
        assertEquals(2, snapshot.getCandidateCount());
        assertEquals(2, BasisController.lastTournamentCandidatesSnapshot().size());
        assertEquals("winner-miner", snapshot.getSelectedWinnerMinerAddress());
        assertEquals("winner-hash", snapshot.getSelectedWinnerHash());
        assertEquals(42, snapshot.getSelectedWinnerBigRandom());
        assertEquals(9, snapshot.getSelectedWinnerTxCount());
        assertEquals(100L, snapshot.getSelectedWinnerDifficulty());
    }

    @Test
    void selectedWinnerCreatesSnapshotWhenCandidateListIsEmpty() {
        LiteVersionWiner selected = lite(514300L, "solo-miner", "solo-hash", 3, 11, 200);

        BasisController.publishTournamentSnapshot(new ArrayList<>(), selected, 1000L, 2000L,
                "tournament after addBlock3", "local_selected_winner_no_candidate_snapshot");

        TournamentSnapshot snapshot = BasisController.getLastTournamentSnapshot();

        assertNotNull(snapshot);
        assertEquals(514300L, snapshot.getBlockIndex());
        assertEquals(1, snapshot.getCandidateCount());
        assertEquals(1, snapshot.getCandidates().size());
        assertEquals("solo-miner", snapshot.getSelectedWinnerMinerAddress());
        assertEquals("solo-hash", snapshot.getSelectedWinnerHash());
        assertEquals("local_selected_winner_no_candidate_snapshot", snapshot.getSelectionReason());
    }

    @Test
    void selectedWinnerRemainsAvailableAfterTemporaryStateClear() {
        LiteVersionWiner selected = lite(514301L, "persisted-miner", "persisted-hash", 1, 5, 55);
        CopyOnWriteArrayList<Block> candidates = new CopyOnWriteArrayList<>();
        candidates.add(block(514301L, "temporary-miner", "temporary-hash"));
        BasisController.setWinnerList(candidates);
        BasisController.setSizeWinnerList(candidates.size());

        BasisController.publishTournamentSnapshot(new ArrayList<>(), selected, 1000L, 2000L,
                "tournament after addBlock3", "local_selected_winner_no_candidate_snapshot");
        new TournamentService().clearTemporaryTournamentState();

        assertTrue(BasisController.currentCandidatesSnapshot().isEmpty());
        assertEquals(1, BasisController.lastTournamentCandidatesSnapshot().size());
        assertEquals("persisted-miner", BasisController.getLastTournamentSnapshot().getSelectedWinnerMinerAddress());
    }

    @Test
    void allWinnersAndLastTournamentWinnerUsePersistedSnapshotAfterCleanup() {
        LiteVersionWiner selected = lite(514302L, "endpoint-miner", "endpoint-hash", 2, 7, 77);
        BasisController.publishTournamentSnapshot(new ArrayList<>(), selected, 1000L, 2000L,
                "tournament after addBlock3", "local_selected_winner_no_candidate_snapshot");
        new TournamentService().clearTemporaryTournamentState();

        BasisController controller = new BasisController();

        assertEquals(1, controller.allWinners().size());
        assertEquals("endpoint-miner", ((LiteVersionWiner) controller.lastTournamentWinner()).getAddress());
    }

    @Test
    void selectedWinnerIsIncludedWhenMissingFromFullCandidateList() {
        List<LiteVersionWiner> ranked = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ranked.add(lite(514303L, "candidate-" + i, "hash-" + i, i, i, i));
        }
        LiteVersionWiner selected = lite(514303L, "selected-miner", "selected-hash", 4, 9, 99);

        BasisController.publishTournamentSnapshot(ranked, selected, 1000L, 2000L,
                "tournament after addBlock3", "ranked_by_tournament_sort");

        TournamentSnapshot snapshot = BasisController.getLastTournamentSnapshot();

        assertEquals(100, snapshot.getCandidateCount());
        assertEquals("selected-miner", snapshot.getCandidates().get(0).getAddress());
        assertEquals("selected-miner", snapshot.getSelectedWinnerMinerAddress());
        assertFalse(snapshot.getCandidates().stream().anyMatch(candidate -> "candidate-99".equals(candidate.getAddress())));
    }

    @Test
    void snapshotStoresLiteDtoOnlyNotFullBlockTransactions() {
        LiteVersionWiner selected = lite(514304L, "dto-miner", "dto-hash", 12, 3, 33);

        BasisController.publishTournamentSnapshot(new ArrayList<>(), selected, 1000L, 2000L,
                "tournament after addBlock3", "local_selected_winner_no_candidate_snapshot");

        TournamentSnapshot snapshot = BasisController.getLastTournamentSnapshot();

        assertEquals(LiteVersionWiner.class, snapshot.getSelectedWinner().getClass());
        assertEquals(12, snapshot.getSelectedWinner().getCountTransactions());
        assertFalse(String.valueOf(snapshot).contains("dtoTransactions"));
    }

    @Test
    void clearTemporaryTournamentStateDoesNotClearPublicSnapshot() {
        CopyOnWriteArrayList<Block> candidates = new CopyOnWriteArrayList<>();
        candidates.add(block(514292L, "winner-miner", "winner-hash"));
        BasisController.setWinnerList(candidates);
        BasisController.setAllWiners(new ArrayList<>(List.of(lite(514292L, "winner-miner", "winner-hash", 1, 10, 5))));
        BasisController.publishTournamentSnapshot(BasisController.getAllWiners(), 1000L, 2000L,
                "tournament after sortWinner", "ranked_by_tournament_sort");

        new TournamentService().clearTemporaryTournamentState();

        assertTrue(BasisController.currentCandidatesSnapshot().isEmpty());
        assertEquals(1, BasisController.lastTournamentCandidatesSnapshot().size());
        assertEquals("winner-miner", BasisController.getLastTournamentSnapshot().getSelectedWinnerMinerAddress());
    }

    private static Block block(long index, String miner, String hash) {
        Block block = new Block();
        block.setIndex(index);
        block.setMinerAddress(miner);
        block.setHashBlock(hash);
        block.setHashCompexity(100L);
        block.setDtoTransactions(new ArrayList<>());
        return block;
    }

    private static LiteVersionWiner lite(long index, String miner, String hash, int txCount, int bigRandom, long diff) {
        return new LiteVersionWiner(index, miner, hash, txCount, 0.0, bigRandom, diff);
    }

    private static void resetTournamentSnapshots() {
        try {
            Method reset = BasisController.class.getDeclaredMethod("resetTournamentSnapshotsForTests");
            reset.setAccessible(true);
            reset.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to reset tournament snapshots", e);
        }
    }
}
