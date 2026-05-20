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
import International_Trade_Union.utils.UtilsBlock;
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

public class MiningV35JackpotControllerStorageIntegrationTest {
    private static final long FORK_INDEX = 5_000L;
    private static final String FOUNDER = "founder-address";
    private static final String PAYOUT_MINER = "payout-miner";
    private static final BigDecimal STARTING_STAKE = new BigDecimal("1000.00");
    private static final BigDecimal SPEND_AMOUNT = new BigDecimal("100.00");

    @Test
    public void disabledForkKeepsControllerStorageJackpotPathInactive() throws Exception {
        Fixture fixture = fixture();

        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertTrue(Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey()).isEmpty());
    }

    @Test
    public void jackpotPayoutSurvivesStorageRoundTripAndLaterSpendInShortCheck() throws Exception {
        Fixture fixture = fixture();
        Keys recipient = UtilsSecurity.generateKeyPair();
        List<DtoTransaction> payoutTransactions = Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX);
        List<DtoTransaction> payoutBlockTransactions = new ArrayList<>();
        payoutBlockTransactions.add(minerReward(PAYOUT_MINER));
        payoutBlockTransactions.addAll(payoutTransactions);
        Block payoutBlock = minedV35Block(
                fixture.payoutIndex,
                fixture.chain.get(fixture.chain.size() - 1).hashForBlockchain(),
                PAYOUT_MINER,
                payoutBlockTransactions);

        String json = UtilsJson.objToStringJson(payoutBlock);
        Block reloadedPayoutBlock = UtilsJson.jsonToBLock(json);

        assertEquals(payoutBlock, reloadedPayoutBlock);
        assertTrue(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, reloadedPayoutBlock, FOUNDER, FORK_INDEX));

        Block spendBlock = minedV35Block(
                fixture.payoutIndex + 1L,
                reloadedPayoutBlock.hashForBlockchain(),
                "next-miner",
                List.of(minerReward("next-miner"), spend(fixture.winnerKeys, recipient.getPubkey(), SPEND_AMOUNT)));
        Map<String, Account> balances = startingBalances(fixture.winnerKeys.getPubkey());
        DataShortBlockchainInformation result = Blockchain.shortCheckWithCanonicalContext(
                fixture.chain.get(fixture.chain.size() - 1),
                List.of(reloadedPayoutBlock, spendBlock),
                new DataShortBlockchainInformation(fixture.payoutIndex - 1L, true, 0L, BigDecimal.ZERO, 0L, 0),
                Collections.emptyList(),
                balances,
                new ArrayList<>(),
                cloneBalances(balances),
                new ArrayList<>(),
                fixture.chain,
                FORK_INDEX);

        assertTrue(result.isValidation());
        assertEquals(fixture.payoutIndex + 1L, result.getSize());
        assertEquals(0, fixture.amount.subtract(SPEND_AMOUNT)
                .compareTo(balances.get(fixture.winnerKeys.getPubkey()).getDigitalDollarBalance()));
        assertEquals(0, SPEND_AMOUNT.compareTo(balances.get(recipient.getPubkey()).getDigitalDollarBalance()));
    }

    @Test
    public void storageStyleValidationRejectsInvalidJackpotPayouts() throws Exception {
        Fixture fixture = fixture();
        DtoTransaction wrongWinner = payout("wrong-winner", fixture.amount.doubleValue());
        Block wrongWinnerBlock = minedV35Block(
                fixture.payoutIndex,
                fixture.chain.get(fixture.chain.size() - 1).hashForBlockchain(),
                PAYOUT_MINER,
                List.of(minerReward(PAYOUT_MINER), wrongWinner));

        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, wrongWinnerBlock, FOUNDER, FORK_INDEX));
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
        return new Fixture(chain, payoutIndex, JackpotCalculator.expectedPayoutAmount(period), winnerKeys);
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

    private static DtoTransaction spend(Keys sender, String recipient, BigDecimal amount) throws Exception {
        DtoTransaction spend = new DtoTransaction(sender.getPubkey(), recipient, amount.doubleValue(), 0.0,
                new Laws(), 0.0, VoteEnum.YES);
        spend.setSign(UtilsSecurity.sign(privateKey(sender), spend.toSign()));
        return spend;
    }

    private static PrivateKey privateKey(Keys keys) throws Exception {
        Base base = new Base58();
        return UtilsSecurity.privateBytToPrivateKey(base.decode(keys.getPrivkey()));
    }

    private static PrivateKey basisPrivateKey() throws Exception {
        Base base = new Base58();
        return UtilsSecurity.privateBytToPrivateKey(base.decode(Seting.BASIS_PASSWORD));
    }

    private static Map<String, Account> startingBalances(String winnerAddress) {
        Map<String, Account> balances = new HashMap<>();
        balances.put(winnerAddress, new Account(winnerAddress, BigDecimal.ZERO, BigDecimal.ZERO, STARTING_STAKE));
        return balances;
    }

    private static Map<String, Account> cloneBalances(Map<String, Account> balances) throws CloneNotSupportedException {
        Map<String, Account> clone = new HashMap<>();
        for (Map.Entry<String, Account> entry : balances.entrySet()) {
            clone.put(entry.getKey(), entry.getValue().clone());
        }
        return clone;
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
