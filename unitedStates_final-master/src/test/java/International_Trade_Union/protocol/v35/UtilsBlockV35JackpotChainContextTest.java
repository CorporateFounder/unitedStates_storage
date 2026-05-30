package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsBlock;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsBlockV35JackpotChainContextTest {
    private static final long FORK_INDEX = 3_000L;
    private static final String FOUNDER = "founder-address";

    @Test
    public void explicitChainContextHelperAcceptsValidJackpotProof() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        JackpotProofResult result = UtilsBlock.validateV35JackpotPayoutWithChainContext(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction, FOUNDER, FORK_INDEX);

        assertTrue(result.isValid(), result.getReason());
        assertEquals(fixture.winner, result.getActualRecipient());
    }

    @Test
    public void missingContextRejectsAmbiguousJackpotPayout() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        JackpotProofResult result = UtilsBlock.validateV35JackpotPayoutWithChainContext(
                Collections.emptyList(), fixture.payoutBlock, fixture.payoutTransaction, FOUNDER, FORK_INDEX);

        assertFalse(result.isValid());
    }

    @Test
    public void explicitChainContextHelperRejectsWrongRecipientImmatureDuplicateAndFounder() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        assertFalse(verifyWithRecipient(fixture, "wrong-recipient").isValid());
        assertFalse(verifyWithRecipient(fixture, FOUNDER).isValid());

        Block immatureBlock = block(fixture.instance.getMaturityBlock(), "payout-miner",
                hash(fixture.instance.getMaturityBlock()),
                Collections.singletonList(payout(fixture.winner, fixture.amount.doubleValue())));
        assertFalse(UtilsBlock.validateV35JackpotPayoutWithChainContext(
                fixture.chain, immatureBlock, immatureBlock.getDtoTransactions().get(0), FOUNDER, FORK_INDEX).isValid());

        List<Block> chainWithDuplicate = new ArrayList<>(fixture.chain);
        chainWithDuplicate.add(fixture.payoutBlock);
        assertFalse(UtilsBlock.validateV35JackpotPayoutWithChainContext(
                chainWithDuplicate, fixture.payoutBlock, fixture.payoutTransaction, FOUNDER, FORK_INDEX).isValid());
    }

    @Test
    public void explicitChainContextHelperRejectsWrongAmountAndDisabledV35() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        DtoTransaction wrongAmount = payout(fixture.winner, fixture.amount.doubleValue() + 0.01);
        Block wrongAmountBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), Collections.singletonList(wrongAmount));

        assertFalse(UtilsBlock.validateV35JackpotPayoutWithChainContext(
                fixture.chain, wrongAmountBlock, wrongAmount, FOUNDER, FORK_INDEX).isValid());

        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertFalse(UtilsBlock.validateV35JackpotPayoutWithChainContext(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction, FOUNDER).isValid());
    }

    @Test
    public void minerInstantRewardIsDistinctFromJackpotPayout() {
        DtoTransaction minerReward = new DtoTransaction(
                Seting.BASIS_ADDRESS,
                "miner",
                V35RewardCalculator.getInstantMinerGoldReward(),
                V35RewardCalculator.getInstantMinerStockReward(),
                new Laws(),
                0.0,
                VoteEnum.YES);
        Block minerRewardBlock = block(FORK_INDEX, "miner", hash(FORK_INDEX), Collections.singletonList(minerReward));

        assertEquals(JackpotCalculator.JackpotPeriod.UNKNOWN,
                JackpotCalculator.classifyPayoutAmount(V35RewardCalculator.getInstantMinerGoldReward()));
        assertTrue(V35RewardCalculator.isValidInstantMinerReward(6.00, 0.00));
        assertTrue(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                Collections.singletonList(block(FORK_INDEX - 1L, "miner-before", hash(FORK_INDEX - 1L), Collections.emptyList())),
                minerRewardBlock,
                FOUNDER,
                FORK_INDEX));
    }

    @Test
    public void blockLevelContextAwareHelperAcceptsValidJackpotPayout() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        assertTrue(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, fixture.payoutBlock, FOUNDER, FORK_INDEX));
    }

    @Test
    public void blockLevelContextAwareHelperRejectsInvalidJackpotPayouts() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                Collections.emptyList(), fixture.payoutBlock, FOUNDER, FORK_INDEX));
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, fixture.payoutBlock, FOUNDER));
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, block(fixture.payoutBlock.getIndex(), "payout-miner", hash(fixture.payoutBlock.getIndex()),
                        Collections.singletonList(payout("wrong-recipient", fixture.amount.doubleValue()))),
                FOUNDER,
                FORK_INDEX));
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, block(fixture.payoutBlock.getIndex(), "payout-miner", hash(fixture.payoutBlock.getIndex()),
                        Collections.singletonList(payout(FOUNDER, fixture.amount.doubleValue()))),
                FOUNDER,
                FORK_INDEX));

        Block immatureBlock = block(fixture.instance.getMaturityBlock(), "payout-miner",
                hash(fixture.instance.getMaturityBlock()),
                Collections.singletonList(payout(fixture.winner, fixture.amount.doubleValue())));
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, immatureBlock, FOUNDER, FORK_INDEX));

        List<Block> chainWithDuplicate = new ArrayList<>(fixture.chain);
        chainWithDuplicate.add(fixture.payoutBlock);
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                chainWithDuplicate, fixture.payoutBlock, FOUNDER, FORK_INDEX));
    }

    @Test
    public void blockLevelContextAwareHelperRejectsDuplicateSamePeriodInBlock() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        List<DtoTransaction> duplicatePayouts = new ArrayList<>();
        duplicatePayouts.add(fixture.payoutTransaction);
        duplicatePayouts.add(payout(fixture.winner, fixture.amount.doubleValue()));
        Block duplicateBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), duplicatePayouts);

        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, duplicateBlock, FOUNDER, FORK_INDEX));
    }

    @Test
    public void blockLevelContextAwareHelperAcceptsMultipleDifferentPeriods() {
        long weeklyWindow = JackpotCalculator.windowBlocksForPeriod(JackpotCalculator.JackpotPeriod.WEEKLY.getPeriodName());
        long weeklyEnd = FORK_INDEX + weeklyWindow - 1L;
        long payoutIndex = weeklyEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L;
        JackpotPeriodInstance daily = V35JackpotChainContextAdapter.buildPeriodInstance(
                JackpotCalculator.JackpotPeriod.DAILY, payoutIndex, FORK_INDEX);
        List<Block> chain = chain(FORK_INDEX, JackpotCalculator.maturityBlock(weeklyEnd, JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS));
        List<DtoTransaction> payouts = V35JackpotPayoutBuilder.buildPayoutTransactions(
                chain, payoutIndex, "payout-miner", FOUNDER, FORK_INDEX);
        Block payoutBlock = block(payoutIndex, "payout-miner", hash(payoutIndex), payouts);

        assertEquals(daily.getFirstPayoutBlock(), payoutIndex);
        assertEquals(2, payouts.size());
        assertTrue(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                chain, payoutBlock, FOUNDER, FORK_INDEX));
    }

    private static JackpotProofResult verifyWithRecipient(Fixture fixture, String recipient) {
        DtoTransaction transaction = payout(recipient, fixture.amount.doubleValue());
        Block payoutBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), Collections.singletonList(transaction));
        return UtilsBlock.validateV35JackpotPayoutWithChainContext(
                fixture.chain, payoutBlock, transaction, FOUNDER, FORK_INDEX);
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
