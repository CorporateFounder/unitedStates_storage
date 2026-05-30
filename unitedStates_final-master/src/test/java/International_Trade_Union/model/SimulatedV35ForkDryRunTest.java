package International_Trade_Union.model;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.protocol.v35.BurnAddressPolicy;
import International_Trade_Union.protocol.v35.ChainTournamentScorer;
import International_Trade_Union.protocol.v35.JackpotCalculator;
import International_Trade_Union.protocol.v35.JackpotPeriodInstance;
import International_Trade_Union.protocol.v35.JackpotWinnerProof;
import International_Trade_Union.protocol.v35.ProtocolVersion;
import International_Trade_Union.protocol.v35.V35JackpotChainContextAdapter;
import International_Trade_Union.protocol.v35.V35JackpotProofService;
import International_Trade_Union.protocol.v35.V35PowValidator;
import International_Trade_Union.protocol.v35.V35RewardCalculator;
import International_Trade_Union.protocol.v35.V35TargetDifficulty;
import International_Trade_Union.protocol.v35.V35WorkCalculator;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsBlock;
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

public class SimulatedV35ForkDryRunTest {
    private static final long FORK_INDEX = 6_000L;
    private static final String FOUNDER = Seting.ADDRESS_FOUNDER;
    private static final String PRE_FORK_MINER = "pre-fork-miner";
    private static final String POST_FORK_MINER = "post-fork-miner";
    private static final String PAYOUT_MINER = "payout-miner";

    @Test
    public void simulatedForkGateDoesNotMutateProductionActivationConstants() {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertFalse(ProtocolVersion.isV35(FORK_INDEX));
        assertFalse(V35JackpotProofService.isV35ActiveAt(FORK_INDEX, Seting.V35_RESCUE_FORK_INDEX));

        assertFalse(V35JackpotProofService.isV35ActiveAt(FORK_INDEX - 1L, FORK_INDEX));
        assertTrue(V35JackpotProofService.isV35ActiveAt(FORK_INDEX, FORK_INDEX));
        assertFalse(BurnAddressPolicy.isNoSpendActive(FORK_INDEX - 1L, FORK_INDEX));
        assertTrue(BurnAddressPolicy.isNoSpendActive(FORK_INDEX, FORK_INDEX));
    }

    @Test
    public void preForkLegacyBlockStillUsesLegacyRewardAndDifficultyRules() throws Exception {
        Block previous = legacyContextBlock(FORK_INDEX - 2L);
        Block legacyBlock = minedLegacyBlock(
                FORK_INDEX - 1L,
                previous.hashForBlockchain(),
                PRE_FORK_MINER,
                List.of(legacyMinerReward(PRE_FORK_MINER)));

        assertTrue(UtilsBlock.validationOneBlockWithCanonicalContext(
                FOUNDER,
                previous,
                legacyBlock,
                Collections.emptyList(),
                null,
                new HashMap<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                Collections.emptyList(),
                FORK_INDEX));
    }

    @Test
    public void forkBlockUsesV35RewardAndTargetValidation() throws Exception {
        Block previous = legacyContextBlock(FORK_INDEX - 1L);
        Block validV35 = minedV35Block(
                FORK_INDEX,
                previous.hashForBlockchain(),
                POST_FORK_MINER,
                List.of(v35MinerReward(POST_FORK_MINER)));
        Block withFounderReward = minedV35Block(
                FORK_INDEX,
                previous.hashForBlockchain(),
                POST_FORK_MINER,
                List.of(v35MinerReward(POST_FORK_MINER), basisTransaction(FOUNDER, 8.0, 8.0)));
        Block withWrongMinerReward = minedV35Block(
                FORK_INDEX,
                previous.hashForBlockchain(),
                POST_FORK_MINER,
                List.of(basisTransaction(POST_FORK_MINER, 7.0, 0.0)));
        Block withInvalidPow = v35BlockWithHash(
                FORK_INDEX,
                previous.hashForBlockchain(),
                POST_FORK_MINER,
                List.of(v35MinerReward(POST_FORK_MINER)),
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

        assertTrue(validate(previous, validV35));
        assertFalse(validate(previous, withFounderReward));
        assertFalse(validate(previous, withWrongMinerReward));
        assertFalse(validate(previous, withInvalidPow));
    }

    @Test
    public void powDifficultyAndCumulativeWorkHelpersMatchV35DryRunPolicy() {
        long compactTarget = V35TargetDifficulty.getInitialCompactTarget();
        BigInteger target = V35TargetDifficulty.decodeCompactTarget(compactTarget);
        BigInteger harderWork = V35WorkCalculator.workFromCompactTarget(
                V35TargetDifficulty.encodeCompactTarget(BigInteger.valueOf(1_000)));
        BigInteger easierWork = V35WorkCalculator.workFromCompactTarget(
                V35TargetDifficulty.encodeCompactTarget(BigInteger.valueOf(10_000)));

        assertEquals(200, V35TargetDifficulty.getTargetBlockTimeSeconds());
        assertTrue(V35PowValidator.isHashUnderTarget(target.toString(16), target));
        assertFalse(V35PowValidator.isHashUnderTarget("ff", BigInteger.ONE));
        assertTrue(harderWork.compareTo(easierWork) > 0);
        assertEquals(
                ChainTournamentScorer.score(true, harderWork, FORK_INDEX, 0.0, "0001"),
                ChainTournamentScorer.betterOf(
                        ChainTournamentScorer.score(true, easierWork, FORK_INDEX, 1.0, "0000"),
                        ChainTournamentScorer.score(true, harderWork, FORK_INDEX, 0.0, "0001")));
        assertEquals(compactTarget, V35TargetDifficulty.calculateNextCompactTarget(
                compactTarget,
                V35TargetDifficulty.getTargetBlockTimeSeconds(),
                V35TargetDifficulty.getTargetBlockTimeSeconds(),
                V35TargetDifficulty.getEmergencyDelaySeconds() - 1L));
    }

    @Test
    public void jackpotPayoutBuilderValidationAndAccountingWorkAfterSimulatedFork() throws Exception {
        Fixture fixture = fixture();
        List<DtoTransaction> payouts = Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX);
        Block payoutBlock = minedV35Block(
                fixture.payoutIndex,
                fixture.chain.get(fixture.chain.size() - 1).hashForBlockchain(),
                PAYOUT_MINER,
                withMinerReward(PAYOUT_MINER, payouts));
        Block wrongWinnerBlock = minedV35Block(
                fixture.payoutIndex,
                fixture.chain.get(fixture.chain.size() - 1).hashForBlockchain(),
                PAYOUT_MINER,
                List.of(v35MinerReward(PAYOUT_MINER), basisTransaction("wrong-winner", fixture.amount.doubleValue(), 0.0)));

        assertEquals(1, payouts.size());
        assertEquals(fixture.winnerKeys.getPubkey(), payouts.get(0).getCustomer());
        assertEquals(0, fixture.amount.compareTo(BigDecimal.valueOf(payouts.get(0).getDigitalDollar())));
        assertTrue(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(fixture.chain, payoutBlock, FOUNDER, FORK_INDEX));
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(fixture.chain, wrongWinnerBlock, FOUNDER, FORK_INDEX));

        Map<String, Account> balances = new HashMap<>();
        DataShortBlockchainInformation result = Blockchain.shortCheckWithCanonicalContext(
                fixture.chain.get(fixture.chain.size() - 1),
                Collections.singletonList(payoutBlock),
                new DataShortBlockchainInformation(fixture.payoutIndex - 1L, true, 0L, BigDecimal.ZERO, 0L, 0),
                Collections.emptyList(),
                balances,
                new ArrayList<>(),
                new HashMap<>(),
                new ArrayList<>(),
                fixture.chain,
                FORK_INDEX);

        assertTrue(result.isValidation());
        assertEquals(0, fixture.amount.compareTo(
                balances.get(fixture.winnerKeys.getPubkey()).getDigitalDollarBalance()));

        List<Block> duplicateContext = new ArrayList<>(fixture.chain);
        duplicateContext.add(payoutBlock);
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                duplicateContext, payoutBlock, FOUNDER, FORK_INDEX));
    }

    @Test
    public void burnAddressNoSpendIsForkGatedAndRecipientRemainsAllowed() {
        assertFalse(BurnAddressPolicy.isSpendForbidden(FORK_INDEX - 1L, Seting.V35_BURN_ADDRESS, FORK_INDEX));
        assertTrue(BurnAddressPolicy.isSpendForbidden(FORK_INDEX, Seting.V35_BURN_ADDRESS, FORK_INDEX));
        assertFalse(BurnAddressPolicy.isSpendForbidden(FORK_INDEX, "ordinary-sender", FORK_INDEX));
        assertTrue(BurnAddressPolicy.isBurnAddress(Seting.V35_BURN_ADDRESS));
    }

    private static boolean validate(Block previous, Block block) throws Exception {
        return UtilsBlock.validationOneBlockWithCanonicalContext(
                FOUNDER,
                previous,
                block,
                Collections.emptyList(),
                null,
                new HashMap<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                Collections.emptyList(),
                FORK_INDEX);
    }

    private static Fixture fixture() throws Exception {
        Keys winnerKeys = UtilsSecurity.generateKeyPair();
        JackpotCalculator.JackpotPeriod period = JackpotCalculator.JackpotPeriod.DAILY;
        long window = JackpotCalculator.windowBlocksForPeriod(period.getPeriodName());
        long periodEnd = FORK_INDEX + window - 1L;
        long payoutIndex = periodEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L;
        JackpotPeriodInstance instance = V35JackpotChainContextAdapter.buildPeriodInstance(period, payoutIndex, FORK_INDEX);
        List<Block> chain = chain(FORK_INDEX, instance.getMaturityBlock(), winnerKeys.getPubkey());
        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(),
                instance.getPeriodEnd(),
                V35JackpotChainContextAdapter.collectEligibleTickets(chain, instance, FORK_INDEX),
                V35JackpotChainContextAdapter.collectFutureHashes(chain, instance));

        assertNotNull(proof);
        assertEquals(winnerKeys.getPubkey(), proof.getWinnerMinerAddress());
        return new Fixture(chain, payoutIndex, JackpotCalculator.expectedPayoutAmount(period), winnerKeys);
    }

    private static List<Block> chain(long from, long to, String minerAddress) {
        List<Block> blocks = new ArrayList<>();
        for (long i = from; i <= to; i++) {
            blocks.add(new Block(new ArrayList<>(), "previous-" + i, minerAddress, FOUNDER,
                    0L, 0.0, V35TargetDifficulty.getInitialCompactTarget(), timestamp(i), i, hash(i)));
        }
        return blocks;
    }

    private static Block legacyContextBlock(long index) {
        return new Block(new ArrayList<>(), "previous-" + index, PRE_FORK_MINER, FOUNDER,
                0L, 1.0, 1L, timestamp(index), index, hash(index));
    }

    private static Block minedLegacyBlock(long index,
                                          String previousHash,
                                          String minerAddress,
                                          List<DtoTransaction> transactions) throws Exception {
        for (long proof = 0; proof < 100_000L; proof++) {
            Block block = new Block(new ArrayList<>(transactions), previousHash, minerAddress, FOUNDER,
                    proof, 1.0, 1L, timestamp(index), index, "");
            block.setHashBlock(block.hashForTransaction());
            if (block.getHashBlock().startsWith("0")) {
                return block;
            }
        }
        throw new IllegalStateException("Unable to mine legacy test block");
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
        throw new IllegalStateException("Unable to mine V35 test block");
    }

    private static Block v35BlockWithHash(long index,
                                          String previousHash,
                                          String minerAddress,
                                          List<DtoTransaction> transactions,
                                          String hashBlock) {
        return new Block(new ArrayList<>(transactions), previousHash, minerAddress, FOUNDER,
                0L, 0.0, V35TargetDifficulty.getInitialCompactTarget(), timestamp(index), index, hashBlock);
    }

    private static List<DtoTransaction> withMinerReward(String minerAddress, List<DtoTransaction> payouts) throws Exception {
        List<DtoTransaction> transactions = new ArrayList<>();
        transactions.add(v35MinerReward(minerAddress));
        transactions.addAll(payouts);
        return transactions;
    }

    private static DtoTransaction legacyMinerReward(String minerAddress) throws Exception {
        return basisTransaction(minerAddress, Seting.DIGITAL_DOLLAR_REWARDS_BEFORE, Seting.DIGITAL_STOCK_REWARDS_BEFORE);
    }

    private static DtoTransaction v35MinerReward(String minerAddress) throws Exception {
        return basisTransaction(minerAddress, V35RewardCalculator.getInstantMinerGoldReward(), 0.0);
    }

    private static DtoTransaction basisTransaction(String recipient, double gold, double stock) throws Exception {
        DtoTransaction transaction = new DtoTransaction(Seting.BASIS_ADDRESS, recipient, gold, stock, new Laws(), 0.0, VoteEnum.YES);
        transaction.setSign(UtilsSecurity.sign(basisPrivateKey(), transaction.toSign()));
        return transaction;
    }

    private static PrivateKey basisPrivateKey() throws Exception {
        Base base = new Base58();
        return UtilsSecurity.privateBytToPrivateKey(base.decode(Seting.BASIS_PASSWORD));
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

        private Fixture(List<Block> chain, long payoutIndex, BigDecimal amount, Keys winnerKeys) {
            this.chain = chain;
            this.payoutIndex = payoutIndex;
            this.amount = amount;
            this.winnerKeys = winnerKeys;
        }
    }
}
