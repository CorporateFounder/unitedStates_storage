package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChainTournamentScorerTest {
    @Test
    public void validChainBeatsInvalidChain() {
        ChainTournamentScorer.ChainScore valid = ChainTournamentScorer.score(true, BigInteger.ONE, 1L, 0.0, "b");
        ChainTournamentScorer.ChainScore invalid = ChainTournamentScorer.score(false, BigInteger.TEN, 100L, 1.0, "a");

        assertTrue(ChainTournamentScorer.compare(valid, invalid) > 0);
        assertSame(valid, ChainTournamentScorer.betterOf(valid, invalid));
    }

    @Test
    public void higherCumulativeWorkBeatsHigherTieBreaker() {
        ChainTournamentScorer.ChainScore higherWork = ChainTournamentScorer.score(true, BigInteger.TEN, 10L, 0.0, "b");
        ChainTournamentScorer.ChainScore higherTieBreaker = ChainTournamentScorer.score(true, BigInteger.ONE, 10L, 999.0, "a");

        assertTrue(ChainTournamentScorer.compare(higherWork, higherTieBreaker) > 0);
    }

    @Test
    public void equalWorkUsesHigherHeight() {
        ChainTournamentScorer.ChainScore taller = ChainTournamentScorer.score(true, BigInteger.TEN, 11L, 0.0, "b");
        ChainTournamentScorer.ChainScore shorter = ChainTournamentScorer.score(true, BigInteger.TEN, 10L, 1.0, "a");

        assertTrue(ChainTournamentScorer.compare(taller, shorter) > 0);
    }

    @Test
    public void equalWorkAndHeightUsesCappedTieBreaker() {
        ChainTournamentScorer.ChainScore higherTieBreaker = ChainTournamentScorer.score(true, BigInteger.TEN, 10L, 0.7, "b");
        ChainTournamentScorer.ChainScore lowerTieBreaker = ChainTournamentScorer.score(true, BigInteger.TEN, 10L, 0.3, "a");

        assertTrue(ChainTournamentScorer.compare(higherTieBreaker, lowerTieBreaker) > 0);
    }

    @Test
    public void finalTieBreakerUsesDeterministicTipHash() {
        ChainTournamentScorer.ChainScore lowerHash = ChainTournamentScorer.score(true, BigInteger.TEN, 10L, 0.5, "0001");
        ChainTournamentScorer.ChainScore higherHash = ChainTournamentScorer.score(true, BigInteger.TEN, 10L, 0.5, "ffff");

        assertTrue(ChainTournamentScorer.compare(lowerHash, higherHash) > 0);
    }

    @Test
    public void hugeTieBreakerCannotBeatHigherCumulativeWork() {
        ChainTournamentScorer.ChainScore higherWork = ChainTournamentScorer.score(true, BigInteger.valueOf(2), 1L, 0.0, "ffff");
        ChainTournamentScorer.ChainScore hugeTieBreaker = ChainTournamentScorer.score(true, BigInteger.ONE, 1L, Double.POSITIVE_INFINITY, "0001");

        assertEquals(1.0, hugeTieBreaker.getCappedTieBreakerScore());
        assertTrue(ChainTournamentScorer.compare(higherWork, hugeTieBreaker) > 0);
    }
}
