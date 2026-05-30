package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35JackpotChainContextAdapterTest {
    private static final long FORK_INDEX = 2_000L;
    private static final String FOUNDER = "founder-address";

    @Test
    public void eligibleTicketExtractionUsesOnlyV35BlocksWithMinerAddress() {
        long dailyEnd = FORK_INDEX + JackpotCalculator.dailyWindowBlocks() - 1L;
        JackpotPeriodInstance instance = new JackpotPeriodInstance(
                JackpotCalculator.JackpotPeriod.DAILY,
                FORK_INDEX,
                dailyEnd,
                dailyEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS,
                dailyEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L);
        List<Block> chain = chain(FORK_INDEX - 2L, dailyEnd, -1L, FORK_INDEX + 5L);

        List<JackpotTicket> tickets = V35JackpotChainContextAdapter.collectEligibleTickets(chain, instance, FORK_INDEX);

        assertTrue(tickets.isEmpty());

        List<Block> cleanChain = chain(FORK_INDEX - 2L, dailyEnd, -1L, -1L);
        List<JackpotTicket> cleanTickets = V35JackpotChainContextAdapter.collectEligibleTickets(cleanChain, instance, FORK_INDEX);

        assertEquals(JackpotCalculator.dailyWindowBlocks(), cleanTickets.size());
        assertEquals(FORK_INDEX, cleanTickets.get(0).getBlockIndex());
        assertEquals(dailyEnd, cleanTickets.get(cleanTickets.size() - 1).getBlockIndex());
    }

    @Test
    public void futureHashExtractionRequiresSixtyOrderedHashes() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        List<String> hashes = V35JackpotChainContextAdapter.collectFutureHashes(fixture.chain, fixture.instance);
        List<String> insufficient = V35JackpotChainContextAdapter.collectFutureHashes(
                chain(FORK_INDEX, fixture.instance.getMaturityBlock() - 1L, -1L, -1L), fixture.instance);
        List<String> invalid = V35JackpotChainContextAdapter.collectFutureHashes(
                chain(FORK_INDEX, fixture.instance.getMaturityBlock(), fixture.instance.getPeriodEnd() + 4L, -1L), fixture.instance);

        assertEquals(JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS, hashes.size());
        assertEquals(hash(fixture.instance.getPeriodEnd() + 1L), hashes.get(0));
        assertEquals(hash(fixture.instance.getMaturityBlock()), hashes.get(hashes.size() - 1));
        assertTrue(insufficient.isEmpty());
        assertTrue(invalid.isEmpty());
    }

    @Test
    public void duplicateScanDetectsSamePeriodButNotDifferentPeriod() {
        Fixture daily = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        Fixture weekly = fixture(JackpotCalculator.JackpotPeriod.WEEKLY);
        List<Block> samePeriodAlreadyPaid = new ArrayList<>(daily.chain);
        samePeriodAlreadyPaid.add(daily.payoutBlock);
        List<Block> differentPeriodPaid = new ArrayList<>(daily.chain);
        differentPeriodPaid.add(weekly.payoutBlock);

        assertFalse(V35JackpotChainContextAdapter.hasPriorPayoutForPeriod(
                daily.chain, daily.instance, daily.payoutBlock.getIndex(), FORK_INDEX));
        assertTrue(V35JackpotChainContextAdapter.hasPriorPayoutForPeriod(
                samePeriodAlreadyPaid, daily.instance, daily.payoutBlock.getIndex(), FORK_INDEX));
        assertFalse(V35JackpotChainContextAdapter.hasPriorPayoutForPeriod(
                differentPeriodPaid, daily.instance, daily.payoutBlock.getIndex(), FORK_INDEX));
    }

    @Test
    public void validDailyChainContextProofPasses() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        JackpotProofResult result = V35JackpotChainContextAdapter.verifyPayoutWithChainContext(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction, FOUNDER, FORK_INDEX);

        assertTrue(result.isValid(), result.getReason());
        assertEquals(fixture.winner, result.getActualRecipient());
        assertEquals(fixture.instance.getDuplicateKey(), result.getPeriodInstance().getDuplicateKey());
    }

    @Test
    public void wrongRecipientImmatureWrongAmountFounderAndSenderBasedWinnerAreRejected() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        assertFalse(verifyWithRecipient(fixture, "not-winner").isValid());
        assertFalse(verifyWithAmount(fixture, fixture.amount.doubleValue() + 0.01).isValid());
        assertFalse(verifyWithRecipient(fixture, FOUNDER).isValid());
        assertFalse(verifyWithRecipient(fixture, "transaction-sender").isValid());

        Block immatureBlock = block(fixture.instance.getMaturityBlock(), "payout-miner",
                hash(fixture.instance.getMaturityBlock()),
                Collections.singletonList(payout(fixture.winner, fixture.amount.doubleValue())));
        assertFalse(V35JackpotChainContextAdapter.verifyPayoutWithChainContext(
                fixture.chain, immatureBlock, immatureBlock.getDtoTransactions().get(0), FOUNDER, FORK_INDEX).isValid());
    }

    @Test
    public void duplicatePayoutContextIsRejected() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        List<Block> chainWithPriorPayout = new ArrayList<>(fixture.chain);
        chainWithPriorPayout.add(fixture.payoutBlock);

        JackpotProofResult result = V35JackpotChainContextAdapter.verifyPayoutWithChainContext(
                chainWithPriorPayout, fixture.payoutBlock, fixture.payoutTransaction, FOUNDER, FORK_INDEX);

        assertFalse(result.isValid());
    }

    @Test
    public void adapterBuildsCandidateAndPeriodInstance() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        JackpotPayoutCandidate candidate = V35JackpotChainContextAdapter.buildPayoutCandidate(
                fixture.payoutBlock, fixture.payoutTransaction);
        JackpotPeriodInstance instance = V35JackpotChainContextAdapter.buildPeriodInstance(candidate, FORK_INDEX);
        Set<String> markers = V35JackpotChainContextAdapter.collectPriorPayoutMarkers(
                Collections.singletonList(fixture.payoutBlock), fixture.payoutBlock.getIndex(), FORK_INDEX);

        assertEquals(JackpotCalculator.JackpotPeriod.DAILY, candidate.getPeriodType());
        assertEquals(fixture.payoutBlock.getIndex(), candidate.getPayoutBlockIndex());
        assertEquals(fixture.instance.getDuplicateKey(), instance.getDuplicateKey());
        assertTrue(markers.contains(fixture.instance.getDuplicateKey()));
    }

    @Test
    public void productionForkGateRemainsDisabled() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertFalse(V35JackpotChainContextAdapter.verifyPayoutWithChainContext(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction, FOUNDER).isValid());
    }

    private static JackpotProofResult verifyWithRecipient(Fixture fixture, String recipient) {
        DtoTransaction transaction = payout(recipient, fixture.amount.doubleValue());
        Block payoutBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), Collections.singletonList(transaction));
        return V35JackpotChainContextAdapter.verifyPayoutWithChainContext(
                fixture.chain, payoutBlock, transaction, FOUNDER, FORK_INDEX);
    }

    private static JackpotProofResult verifyWithAmount(Fixture fixture, double amount) {
        DtoTransaction transaction = payout(fixture.winner, amount);
        Block payoutBlock = block(fixture.payoutBlock.getIndex(), "payout-miner",
                hash(fixture.payoutBlock.getIndex()), Collections.singletonList(transaction));
        return V35JackpotChainContextAdapter.verifyPayoutWithChainContext(
                fixture.chain, payoutBlock, transaction, FOUNDER, FORK_INDEX);
    }

    private static Fixture fixture(JackpotCalculator.JackpotPeriod periodType) {
        long window = JackpotCalculator.windowBlocksForPeriod(periodType.getPeriodName());
        long periodEnd = FORK_INDEX + window - 1L;
        long payoutIndex = periodEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L;
        JackpotPeriodInstance instance = V35JackpotChainContextAdapter.buildPeriodInstance(
                periodType, payoutIndex, FORK_INDEX);
        List<Block> chain = chain(FORK_INDEX, instance.getMaturityBlock(), -1L, -1L);
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

    private static List<Block> chain(long from, long to, long blankHashIndex, long blankMinerIndex) {
        List<Block> blocks = new ArrayList<>();
        for (long i = from; i <= to; i++) {
            String miner = i == blankMinerIndex ? "" : "miner-" + i;
            String blockHash = i == blankHashIndex ? "" : hash(i);
            blocks.add(block(i, miner, blockHash, Collections.emptyList()));
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
