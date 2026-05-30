package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Account;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsBalance;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsBalanceV35JackpotAccountingTest {
    private static final long FORK_INDEX = 4_000L;
    private static final String FOUNDER = "founder-address";

    @Test
    public void validJackpotPayoutIncreasesWinnerGoldBalance() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        Map<String, Account> balances = new HashMap<>();

        boolean applied = UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                balances, chainWithPayout(fixture), fixture.payoutBlock, FOUNDER, FORK_INDEX);

        assertTrue(applied);
        assertEquals(0, fixture.amount.compareTo(
                balances.get(fixture.winner).getDigitalDollarBalance()));
    }

    @Test
    public void wrongImmatureDuplicateFounderAndDisabledPayoutsDoNotIncreaseBalance() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        assertFalse(appliesWithRecipient(fixture, "wrong-recipient"));
        assertFalse(appliesWithRecipient(fixture, FOUNDER));
        assertFalse(appliesAtIndex(fixture, fixture.instance.getMaturityBlock()));
        assertFalse(appliesWithDuplicateSamePeriodInBlock(fixture));

        Map<String, Account> disabledBalances = new HashMap<>();
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertFalse(UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                disabledBalances, chainWithPayout(fixture), fixture.payoutBlock,
                FOUNDER, Seting.V35_RESCUE_FORK_INDEX));
        assertFalse(disabledBalances.containsKey(fixture.winner));
    }

    @Test
    public void minerRewardAndWrongAmountsAreNotJackpotAccountingCandidates() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        DtoTransaction minerReward = payout(fixture.winner, V35RewardCalculator.getInstantMinerGoldReward());
        DtoTransaction wrongAmount = payout(fixture.winner, fixture.amount.doubleValue() + 0.01);
        Block minerRewardBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), Collections.singletonList(minerReward));
        Block wrongAmountBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), Collections.singletonList(wrongAmount));

        assertFalse(UtilsBalance.isValidV35JackpotPayoutForBalance(
                chainWithPayout(fixture), minerRewardBlock, minerReward, FOUNDER, FORK_INDEX));
        assertFalse(UtilsBalance.isValidV35JackpotPayoutForBalance(
                chainWithPayout(fixture), wrongAmountBlock, wrongAmount, FOUNDER, FORK_INDEX));
    }

    private static boolean appliesWithRecipient(Fixture fixture, String recipient) throws Exception {
        Map<String, Account> balances = new HashMap<>();
        DtoTransaction transaction = payout(recipient, fixture.amount.doubleValue());
        Block payoutBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), Collections.singletonList(transaction));
        boolean applied = UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                balances, chainWithPayout(fixture), payoutBlock, FOUNDER, FORK_INDEX);
        return applied || balances.containsKey(recipient);
    }

    private static boolean appliesAtIndex(Fixture fixture, long payoutIndex) throws Exception {
        Map<String, Account> balances = new HashMap<>();
        Block payoutBlock = block(payoutIndex, "payout-miner",
                hash(payoutIndex), Collections.singletonList(fixture.payoutTransaction));
        return UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                balances, chainWithPayout(fixture), payoutBlock, FOUNDER, FORK_INDEX);
    }

    private static boolean appliesWithDuplicateSamePeriodInBlock(Fixture fixture) throws Exception {
        Map<String, Account> balances = new HashMap<>();
        List<DtoTransaction> transactions = new ArrayList<>();
        transactions.add(fixture.payoutTransaction);
        transactions.add(payout(fixture.winner, fixture.amount.doubleValue()));
        Block duplicateBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), transactions);
        return UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                balances, chainWithPayout(fixture), duplicateBlock, FOUNDER, FORK_INDEX);
    }

    private static Fixture fixture(JackpotCalculator.JackpotPeriod periodType) {
        long window = JackpotCalculator.windowBlocksForPeriod(periodType.getPeriodName());
        long periodEnd = FORK_INDEX + window - 1L;
        long payoutIndex = periodEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L;
        JackpotPeriodInstance instance = V35JackpotChainContextAdapter.buildPeriodInstance(
                periodType, payoutIndex, FORK_INDEX);
        List<Block> chain = chain(FORK_INDEX, instance.getMaturityBlock());
        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(),
                instance.getPeriodEnd(),
                V35JackpotChainContextAdapter.collectEligibleTickets(chain, instance, FORK_INDEX),
                V35JackpotChainContextAdapter.collectFutureHashes(chain, instance));
        BigDecimal amount = JackpotCalculator.expectedPayoutAmount(periodType);
        DtoTransaction transaction = payout(proof.getWinnerMinerAddress(), amount.doubleValue());
        Block payoutBlock = block(payoutIndex, "payout-miner", hash(payoutIndex), Collections.singletonList(transaction));
        return new Fixture(chain, payoutBlock, transaction, instance, amount, proof.getWinnerMinerAddress());
    }

    private static List<Block> chainWithPayout(Fixture fixture) {
        List<Block> chain = new ArrayList<>(fixture.chain);
        chain.add(fixture.payoutBlock);
        return chain;
    }

    private static List<Block> chain(long from, long to) {
        List<Block> blocks = new ArrayList<>();
        for (long i = from; i <= to; i++) {
            blocks.add(block(i, "miner-" + i, hash(i), Collections.emptyList()));
        }
        return blocks;
    }

    private static Block block(long index, String minerAddress, String blockHash, List<DtoTransaction> transactions) {
        return new Block(new ArrayList<>(transactions), "previous-" + index, minerAddress, FOUNDER,
                0L, 0.0, 1L, new Timestamp(index * 1000L), index, blockHash);
    }

    private static DtoTransaction payout(String recipient, double amount) {
        return new DtoTransaction(Seting.BASIS_ADDRESS, recipient, amount, 0.0, new Laws(), 0.0, VoteEnum.YES);
    }

    private static String hash(long index) {
        return String.format("%064x", index + 1L);
    }

    private static final class Fixture {
        private final List<Block> chain;
        private final Block payoutBlock;
        private final DtoTransaction payoutTransaction;
        private final JackpotPeriodInstance instance;
        private final BigDecimal amount;
        private final String winner;

        private Fixture(List<Block> chain,
                        Block payoutBlock,
                        DtoTransaction payoutTransaction,
                        JackpotPeriodInstance instance,
                        BigDecimal amount,
                        String winner) {
            this.chain = chain;
            this.payoutBlock = payoutBlock;
            this.payoutTransaction = payoutTransaction;
            this.instance = instance;
            this.amount = amount;
            this.winner = winner;
        }
    }
}
