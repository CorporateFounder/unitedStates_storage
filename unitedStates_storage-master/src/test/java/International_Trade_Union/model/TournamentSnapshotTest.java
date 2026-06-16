package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentSnapshotTest {
    @AfterEach
    void tearDown() {
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
}
