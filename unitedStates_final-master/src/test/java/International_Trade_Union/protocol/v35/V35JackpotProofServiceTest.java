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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35JackpotProofServiceTest {
    private static final long FORK_INDEX = 1_000L;
    private static final String FOUNDER = "founder-address";

    @Test
    public void validDailyPayoutProofPasses() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        JackpotProofResult result = V35JackpotProofService.verifyPayout(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        assertTrue(result.isValid(), result.getReason());
        assertEquals(JackpotCalculator.JackpotPeriod.DAILY, result.getPeriodInstance().getPeriodType());
        assertEquals(FORK_INDEX, result.getPeriodInstance().getPeriodStart());
        assertEquals(FORK_INDEX + JackpotCalculator.dailyWindowBlocks() - 1L,
                result.getPeriodInstance().getPeriodEnd());
        assertEquals(fixture.winner, result.getActualRecipient());
        assertEquals(fixture.winner, result.getExpectedWinnerAddress());
        assertEquals(JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.DAILY),
                result.getExpectedAmount());
    }

    @Test
    public void validWeeklyPayoutProofPasses() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.WEEKLY);

        JackpotProofResult result = V35JackpotProofService.verifyPayout(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        assertTrue(result.isValid(), result.getReason());
        assertEquals(JackpotCalculator.JackpotPeriod.WEEKLY, result.getPeriodInstance().getPeriodType());
        assertEquals(FORK_INDEX + JackpotCalculator.weeklyWindowBlocks() - 1L,
                result.getPeriodInstance().getPeriodEnd());
    }

    @Test
    public void wrongRecipientWrongAmountAndImmaturePayoutAreRejected() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        JackpotProofResult wrongRecipient = V35JackpotProofService.verifyPayout(
                fixture.chain,
                block(fixture.payoutBlock.getIndex(), "payout-miner", hash(fixture.payoutBlock.getIndex()),
                        Collections.singletonList(payout("not-the-winner", fixture.amount.doubleValue()))),
                payout("not-the-winner", fixture.amount.doubleValue()),
                FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        JackpotProofResult wrongAmount = V35JackpotProofService.verifyPayout(
                fixture.chain,
                block(fixture.payoutBlock.getIndex(), "payout-miner", hash(fixture.payoutBlock.getIndex()),
                        Collections.singletonList(payout(fixture.winner, fixture.amount.doubleValue() + 0.01))),
                payout(fixture.winner, fixture.amount.doubleValue() + 0.01),
                FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        Block immatureBlock = block(fixture.instance.getMaturityBlock(), "payout-miner",
                hash(fixture.instance.getMaturityBlock()),
                Collections.singletonList(payout(fixture.winner, fixture.amount.doubleValue())));
        JackpotProofResult immature = V35JackpotProofService.verifyPayout(
                fixture.chain, immatureBlock, immatureBlock.getDtoTransactions().get(0),
                FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        assertFalse(wrongRecipient.isValid());
        assertFalse(wrongAmount.isValid());
        assertFalse(immature.isValid());
    }

    @Test
    public void insufficientInvalidOrEmptyTicketContextIsRejected() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        List<Block> insufficient = chain(FORK_INDEX, fixture.instance.getMaturityBlock() - 1L, -1L, -1L);
        List<Block> invalidHash = chain(FORK_INDEX, fixture.instance.getMaturityBlock(), fixture.instance.getPeriodEnd() + 7L, -1L);
        List<Block> blankMiner = chain(FORK_INDEX, fixture.instance.getMaturityBlock(), -1L, fixture.instance.getPeriodStart() + 2L);

        assertFalse(V35JackpotProofService.verifyPayout(insufficient, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, Collections.emptySet(), false, FORK_INDEX).isValid());
        assertFalse(V35JackpotProofService.verifyPayout(invalidHash, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, Collections.emptySet(), false, FORK_INDEX).isValid());
        assertFalse(V35JackpotProofService.verifyPayout(blankMiner, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, Collections.emptySet(), false, FORK_INDEX).isValid());
    }

    @Test
    public void founderAlreadyPaidAndSamePeriodDuplicateAreRejected() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        Block founderBlock = block(fixture.payoutBlock.getIndex(), "payout-miner", hash(fixture.payoutBlock.getIndex()),
                Collections.singletonList(payout(FOUNDER, fixture.amount.doubleValue())));
        JackpotProofResult founder = V35JackpotProofService.verifyPayout(
                fixture.chain, founderBlock, founderBlock.getDtoTransactions().get(0),
                FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        JackpotProofResult alreadyPaidFlag = V35JackpotProofService.verifyPayout(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, Collections.emptySet(), true, FORK_INDEX);

        JackpotProofResult alreadyPaidMarker = V35JackpotProofService.verifyPayout(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, new HashSet<>(Collections.singletonList(fixture.instance.getDuplicateKey())), false, FORK_INDEX);

        DtoTransaction duplicateA = payout(fixture.winner, fixture.amount.doubleValue());
        DtoTransaction duplicateB = payout("another-recipient", fixture.amount.doubleValue());
        Block duplicateBlock = block(fixture.payoutBlock.getIndex(), "payout-miner", hash(fixture.payoutBlock.getIndex()),
                Arrays.asList(duplicateA, duplicateB));
        JackpotProofResult samePeriodDuplicate = V35JackpotProofService.verifyPayout(
                fixture.chain, duplicateBlock, duplicateA, FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        assertFalse(founder.isValid());
        assertFalse(alreadyPaidFlag.isValid());
        assertFalse(alreadyPaidMarker.isValid());
        assertFalse(samePeriodDuplicate.isValid());
    }

    @Test
    public void senderBasedRecipientCannotBecomeWinner() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        DtoTransaction senderBased = payout("transaction-sender", fixture.amount.doubleValue());
        Block payoutBlock = block(fixture.payoutBlock.getIndex(), "payout-miner", hash(fixture.payoutBlock.getIndex()),
                Collections.singletonList(senderBased));

        JackpotProofResult result = V35JackpotProofService.verifyPayout(
                fixture.chain, payoutBlock, senderBased, FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        assertFalse(result.isValid());
        assertNotEquals("transaction-sender", fixture.winner);
    }

    @Test
    public void proofResultIsDeterministicAndFutureHashesAffectWinner() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        JackpotProofResult first = V35JackpotProofService.verifyPayout(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, Collections.emptySet(), false, FORK_INDEX);
        JackpotProofResult second = V35JackpotProofService.verifyPayout(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction,
                FOUNDER, Collections.emptySet(), false, FORK_INDEX);

        assertEquals(first.isValid(), second.isValid());
        assertEquals(first.getWinnerProof(), second.getWinnerProof());

        List<JackpotTicket> tickets = Arrays.asList(
                JackpotTicket.of(1L, "miner-a"),
                JackpotTicket.of(2L, "miner-b"),
                JackpotTicket.of(3L, "miner-c"));
        JackpotWinnerProof base = JackpotCalculator.selectWinner(0L, 2L, tickets, futureHashes("a"));
        JackpotWinnerProof changed = base;
        for (int i = 0; i < 100 && base.getWinnerMinerAddress().equals(changed.getWinnerMinerAddress()); i++) {
            changed = JackpotCalculator.selectWinner(0L, 2L, tickets, futureHashes("b" + i));
        }
        assertNotEquals(base.getWinnerMinerAddress(), changed.getWinnerMinerAddress());
    }

    @Test
    public void periodSafetyAndDisabledGateAreEnforced() {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        assertEquals(JackpotCalculator.JackpotPeriod.DAILY,
                JackpotCalculator.classifyPayoutAmount(fixture.amount.doubleValue()));
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertFalse(V35JackpotProofService.verifyPayout(
                fixture.chain, fixture.payoutBlock, fixture.payoutTransaction, FOUNDER).isValid());

        Block preForkBlock = block(FORK_INDEX - 1L, "payout-miner", hash(FORK_INDEX - 1L),
                Collections.singletonList(payout(fixture.winner, fixture.amount.doubleValue())));
        assertFalse(V35JackpotProofService.verifyPayout(
                fixture.chain, preForkBlock, preForkBlock.getDtoTransactions().get(0),
                FOUNDER, Collections.emptySet(), false, FORK_INDEX).isValid());
    }

    private static Fixture fixture(JackpotCalculator.JackpotPeriod periodType) {
        long window = JackpotCalculator.windowBlocksForPeriod(periodType.getPeriodName());
        long periodEnd = FORK_INDEX + window - 1L;
        long payoutIndex = periodEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L;
        JackpotPeriodInstance instance = V35JackpotProofService.periodInstanceForPayoutBlock(periodType, payoutIndex, FORK_INDEX);
        List<Block> chain = chain(FORK_INDEX, instance.getMaturityBlock(), -1L, -1L);
        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(),
                instance.getPeriodEnd(),
                V35JackpotProofService.ticketsForPeriod(chain, instance, FORK_INDEX),
                V35JackpotProofService.futureHashesForSeed(chain, instance));
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

    private static List<String> futureHashes(String salt) {
        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS; i++) {
            hashes.add(String.format("%064x", Math.abs((salt + i).hashCode()) + 1L));
        }
        return hashes;
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
