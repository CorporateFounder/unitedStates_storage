package International_Trade_Union.model;

import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.protocol.v35.V35TargetDifficulty;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TournamentServiceTest {
    private static final String HASH_ONE = "0000000000000000000000000000000000000000000000000000000000000001";
    private static final String HASH_TWO = "0000000000000000000000000000000000000000000000000000000000000002";
    private static final String HASH_THREE = "0000000000000000000000000000000000000000000000000000000000000003";

    @Test
    public void v35DisabledKeepsLegacyTournamentPriority() {
        long weakerV35WorkButHigherLegacyDifficulty = compactTarget(BigInteger.valueOf(10_000));
        long strongerV35WorkButLowerLegacyDifficulty = compactTarget(BigInteger.valueOf(1_000));
        Block legacyPreferred = block("legacy", weakerV35WorkButHigherLegacyDifficulty, HASH_ONE, 10L);
        Block v35WorkPreferred = block("v35", strongerV35WorkButLowerLegacyDifficulty, HASH_ONE, 10L);
        List<Block> candidates = List.of(v35WorkPreferred, legacyPreferred);

        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertFalse(TournamentService.shouldUseV35CandidatePriority(candidates, Seting.V35_RESCUE_FORK_INDEX));
        assertEquals(legacyPreferred, TournamentService.sortWinner(balances(), candidates).get(0));
    }

    @Test
    public void simulatedV35CandidatePriorityUsesPowBeforeStake() {
        long highWork = compactTarget(BigInteger.valueOf(1_000));
        long lowWork = compactTarget(BigInteger.valueOf(10_000));
        Block powWinner = block("pow", highWork, HASH_TWO, 10L);
        Block richStakeLoser = block("stake", lowWork, HASH_ONE, 10L);
        Map<String, Account> balances = balances(
                account("pow", BigDecimal.ZERO),
                account("stake", BigDecimal.valueOf(999_999)));

        assertTrue(TournamentService.shouldUseV35CandidatePriority(List.of(powWinner, richStakeLoser), 1L));
        assertEquals(powWinner, TournamentService.sortWinnerForForkIndex(balances, List.of(richStakeLoser, powWinner), 1L).get(0));
    }

    @Test
    public void simulatedV35CandidatePriorityUsesLowerHashAsTieBreaker() {
        long sameWork = compactTarget(BigInteger.valueOf(1_000));
        Block lowerHash = block("a", sameWork, HASH_ONE, 10L);
        Block higherHash = block("b", sameWork, HASH_THREE, 10L);

        assertEquals(lowerHash, TournamentService.sortWinnerForForkIndex(balances(), List.of(higherHash, lowerHash), 1L).get(0));
    }

    @Test
    public void invalidV35CandidateDataDoesNotCrash() {
        Block invalidHash = block("bad", compactTarget(BigInteger.valueOf(1_000)), "not-a-hex-hash", 10L);
        Block valid = block("good", compactTarget(BigInteger.valueOf(2_000)), HASH_TWO, 10L);

        assertFalse(TournamentService.shouldUseV35CandidatePriority(List.of(invalidHash, valid), 1L));
        assertDoesNotThrow(() -> TournamentService.sortWinnerForForkIndex(balances(), List.of(invalidHash, valid), 1L));
        assertDoesNotThrow(() -> TournamentService.compareV35Candidate(balances(), invalidHash, valid));
    }

    private static long compactTarget(BigInteger target) {
        return V35TargetDifficulty.encodeCompactTarget(target);
    }

    private static Block block(String miner, long compactTarget, String hash, long index) {
        return new Block(new ArrayList<>(), "previous", miner, "founder",
                0L, 0.0, compactTarget, new Timestamp(1_000L), index, hash);
    }

    private static Map<String, Account> balances(Account... accounts) {
        Map<String, Account> balances = new HashMap<>();
        for (Account account : accounts) {
            balances.put(account.getAccount(), account);
        }
        return balances;
    }

    private static Account account(String address, BigDecimal stake) {
        return new Account(address, BigDecimal.ZERO, BigDecimal.ZERO, stake);
    }
}
