package International_Trade_Union.model;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.protocol.v35.JackpotCalculator;
import International_Trade_Union.protocol.v35.JackpotPeriodInstance;
import International_Trade_Union.protocol.v35.JackpotWinnerProof;
import International_Trade_Union.protocol.v35.V35JackpotChainContextAdapter;
import International_Trade_Union.protocol.v35.V35RewardCalculator;
import International_Trade_Union.protocol.v35.V35TargetDifficulty;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsJson;
import International_Trade_Union.utils.UtilsSecurity;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Phase6B1LiveControllerDatabaseHardeningTest {
    private static final long FORK_INDEX = 6_000L;
    private static final String FOUNDER = "founder-address";
    private static final String PAYOUT_MINER = "payout-miner";

    @Test
    public void contextAwareStorageStyleShortCheckAcceptsReloadedJackpotPayoutBlock() throws Exception {
        Fixture fixture = fixture();
        Block reloadedPayoutBlock = UtilsJson.jsonToBLock(UtilsJson.objToStringJson(fixture.payoutBlock));
        Map<String, Account> balances = new HashMap<>();

        DataShortBlockchainInformation result = Blockchain.shortCheckWithCanonicalContext(
                fixture.chain.get(fixture.chain.size() - 1),
                Collections.singletonList(reloadedPayoutBlock),
                new DataShortBlockchainInformation(fixture.payoutIndex - 1L, true, 0L, BigDecimal.ZERO, 0L, 0),
                Collections.emptyList(),
                balances,
                new ArrayList<>(),
                new HashMap<>(),
                new ArrayList<>(),
                fixture.chain,
                FORK_INDEX);

        assertTrue(result.isValidation());
        assertEquals(fixture.payoutIndex, result.getSize());
        assertEquals(0, fixture.amount.compareTo(
                balances.get(fixture.winnerKeys.getPubkey()).getDigitalDollarBalance()));
    }

    @Test
    public void contextAwareStorageStyleShortCheckRejectsInvalidJackpotPayoutBlock() throws Exception {
        Fixture fixture = fixture();
        Block wrongWinnerBlock = minedV35Block(
                fixture.payoutIndex,
                fixture.chain.get(fixture.chain.size() - 1).hashForBlockchain(),
                PAYOUT_MINER,
                List.of(minerReward(PAYOUT_MINER), payout("wrong-winner", fixture.amount.doubleValue())));

        DataShortBlockchainInformation result = Blockchain.shortCheckWithCanonicalContext(
                fixture.chain.get(fixture.chain.size() - 1),
                Collections.singletonList(wrongWinnerBlock),
                new DataShortBlockchainInformation(fixture.payoutIndex - 1L, true, 0L, BigDecimal.ZERO, 0L, 0),
                Collections.emptyList(),
                new HashMap<>(),
                new ArrayList<>(),
                new HashMap<>(),
                new ArrayList<>(),
                fixture.chain,
                FORK_INDEX);

        assertFalse(result.isValidation());
    }

    @Test
    public void tournamentSelectionStaysLegacyWhileV35IsDisabled() {
        Block legacyPreferred = tournamentBlock("legacy", V35TargetDifficulty.encodeCompactTarget(BigInteger.valueOf(10_000)), 10L);
        Block v35WorkPreferred = tournamentBlock("v35", V35TargetDifficulty.encodeCompactTarget(BigInteger.valueOf(1_000)), 10L);

        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertFalse(TournamentService.shouldUseV35CandidatePriority(
                List.of(v35WorkPreferred, legacyPreferred), Seting.V35_RESCUE_FORK_INDEX));
        assertEquals(legacyPreferred, TournamentService.sortWinner(new HashMap<>(),
                List.of(v35WorkPreferred, legacyPreferred)).get(0));
    }

    @Test
    public void simulatedV35TournamentPrefersCumulativeWorkPath() {
        Block lowWork = tournamentBlock("low-work", V35TargetDifficulty.encodeCompactTarget(BigInteger.valueOf(10_000)), FORK_INDEX);
        Block highWork = tournamentBlock("high-work", V35TargetDifficulty.encodeCompactTarget(BigInteger.valueOf(1_000)), FORK_INDEX);

        assertTrue(TournamentService.shouldUseV35CandidatePriority(List.of(lowWork, highWork), FORK_INDEX));
        assertEquals(highWork, TournamentService.sortWinnerForForkIndex(new HashMap<>(),
                List.of(lowWork, highWork), FORK_INDEX).get(0));
    }

    @Test
    public void v35RulesDoNotAccidentallyActivateWithProductionForkConstant() throws Exception {
        Fixture fixture = fixture();

        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertTrue(Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey()).isEmpty());
    }

    private static Fixture fixture() throws Exception {
        Keys winnerKeys = UtilsSecurity.generateKeyPair();
        JackpotCalculator.JackpotPeriod period = JackpotCalculator.JackpotPeriod.DAILY;
        long window = JackpotCalculator.windowBlocksForPeriod(period.getPeriodName());
        long periodEnd = FORK_INDEX + window - 1L;
        long payoutIndex = periodEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L;
        JackpotPeriodInstance instance = V35JackpotChainContextAdapter.buildPeriodInstance(
                period, payoutIndex, FORK_INDEX);
        List<Block> chain = chain(FORK_INDEX, instance.getMaturityBlock(), winnerKeys.getPubkey());
        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(),
                instance.getPeriodEnd(),
                V35JackpotChainContextAdapter.collectEligibleTickets(chain, instance, FORK_INDEX),
                V35JackpotChainContextAdapter.collectFutureHashes(chain, instance));

        assertNotNull(proof);
        assertEquals(winnerKeys.getPubkey(), proof.getWinnerMinerAddress());

        BigDecimal amount = JackpotCalculator.expectedPayoutAmount(period);
        List<DtoTransaction> payoutTransactions = Mining.buildV35JackpotPayoutTransactionsForMining(
                chain, payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX);
        List<DtoTransaction> payoutBlockTransactions = new ArrayList<>();
        payoutBlockTransactions.add(minerReward(PAYOUT_MINER));
        payoutBlockTransactions.addAll(payoutTransactions);
        Block payoutBlock = minedV35Block(
                payoutIndex,
                chain.get(chain.size() - 1).hashForBlockchain(),
                PAYOUT_MINER,
                payoutBlockTransactions);
        return new Fixture(chain, payoutIndex, amount, winnerKeys, payoutBlock);
    }

    private static List<Block> chain(long from, long to, String minerAddress) {
        List<Block> blocks = new ArrayList<>();
        for (long i = from; i <= to; i++) {
            blocks.add(contextBlock(i, minerAddress, hash(i)));
        }
        return blocks;
    }

    private static Block contextBlock(long index, String minerAddress, String blockHash) {
        return new Block(new ArrayList<>(), "previous-" + index, minerAddress, FOUNDER,
                0L, 0.0, V35TargetDifficulty.getInitialCompactTarget(), timestamp(index), index, blockHash);
    }

    private static Block minedV35Block(long index,
                                       String previousHash,
                                       String minerAddress,
                                       List<DtoTransaction> transactions) throws Exception {
        long compactTarget = V35TargetDifficulty.getInitialCompactTarget();
        BigInteger target = V35TargetDifficulty.decodeCompactTarget(compactTarget);
        for (long proof = 0; proof < 10_000L; proof++) {
            Block block = new Block(new ArrayList<>(transactions), previousHash, minerAddress, FOUNDER,
                    proof, 0.0, compactTarget, timestamp(index), index, "");
            block.setHashBlock(block.hashForTransaction());
            if (new BigInteger(block.getHashBlock(), 16).compareTo(target) <= 0) {
                return block;
            }
        }
        throw new IllegalStateException("Unable to mine test V35 block under initial target");
    }

    private static DtoTransaction minerReward(String minerAddress) throws Exception {
        DtoTransaction reward = new DtoTransaction(Seting.BASIS_ADDRESS, minerAddress,
                V35RewardCalculator.getInstantMinerGoldReward(), 0.0, new Laws(), 0.0, VoteEnum.YES);
        reward.setSign(UtilsSecurity.sign(basisPrivateKey(), reward.toSign()));
        return reward;
    }

    private static DtoTransaction payout(String recipient, double amount) throws Exception {
        DtoTransaction payout = new DtoTransaction(Seting.BASIS_ADDRESS, recipient, amount, 0.0, new Laws(), 0.0, VoteEnum.YES);
        payout.setSign(UtilsSecurity.sign(basisPrivateKey(), payout.toSign()));
        return payout;
    }

    private static PrivateKey basisPrivateKey() throws Exception {
        Base base = new Base58();
        return UtilsSecurity.privateBytToPrivateKey(base.decode(Seting.BASIS_PASSWORD));
    }

    private static Block tournamentBlock(String miner, long compactTarget, long index) {
        String hash = miner.equals("high-work")
                ? "0000000000000000000000000000000000000000000000000000000000000001"
                : "0000000000000000000000000000000000000000000000000000000000000002";
        return new Block(new ArrayList<>(), "previous", miner, FOUNDER,
                0L, 0.0, compactTarget, timestamp(index), index, hash);
    }

    private static Timestamp timestamp(long index) {
        return new Timestamp(index * 200_000L);
    }

    private static String hash(long index) {
        return String.format("%064x", index + 1L);
    }

    private static final class Fixture {
        private final List<Block> chain;
        private final long payoutIndex;
        private final BigDecimal amount;
        private final Keys winnerKeys;
        private final Block payoutBlock;

        private Fixture(List<Block> chain, long payoutIndex, BigDecimal amount, Keys winnerKeys, Block payoutBlock) {
            this.chain = chain;
            this.payoutIndex = payoutIndex;
            this.amount = amount;
            this.winnerKeys = winnerKeys;
            this.payoutBlock = payoutBlock;
        }
    }
}
