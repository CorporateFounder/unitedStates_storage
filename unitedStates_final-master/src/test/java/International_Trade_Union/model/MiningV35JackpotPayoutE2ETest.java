package International_Trade_Union.model;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.protocol.v35.JackpotCalculator;
import International_Trade_Union.protocol.v35.JackpotPeriodInstance;
import International_Trade_Union.protocol.v35.JackpotProofResult;
import International_Trade_Union.protocol.v35.JackpotWinnerProof;
import International_Trade_Union.protocol.v35.V35JackpotChainContextAdapter;
import International_Trade_Union.protocol.v35.V35RewardCalculator;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsBalance;
import International_Trade_Union.utils.UtilsBlock;
import International_Trade_Union.utils.UtilsSecurity;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MiningV35JackpotPayoutE2ETest {
    private static final long FORK_INDEX = 5_000L;
    private static final String FOUNDER = "founder-address";
    private static final String PAYOUT_MINER = "payout-miner";
    private static final String SENDER_ADDRESS = "sender-address";

    @Test
    public void disabledForkKeepsMiningJackpotPayoutsInactive() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertTrue(Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey()).isEmpty());
    }

    @Test
    public void miningCreatedDailyPayoutPassesContextValidationAndCreditsWinnerBalance() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        List<DtoTransaction> payouts = Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX);
        Block payoutBlock = block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex), payouts);
        DtoTransaction payout = payouts.get(0);

        assertEquals(1, payouts.size());
        assertEquals(Seting.BASIS_ADDRESS, payout.getSender());
        assertEquals(fixture.winner, payout.getCustomer());
        assertEquals(0.0, payout.getDigitalStockBalance());
        assertEquals(0, BigDecimal.valueOf(payout.getDigitalDollar()).compareTo(fixture.amount));
        assertFalse(JackpotCalculator.isCanonicalPayoutAmount(V35RewardCalculator.getInstantMinerGoldReward()));

        JackpotProofResult proof = UtilsBlock.validateV35JackpotPayoutWithChainContext(
                fixture.chain, payoutBlock, payout, FOUNDER, FORK_INDEX);
        assertTrue(proof.isValid(), proof.getReason());
        assertTrue(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, payoutBlock, FOUNDER, FORK_INDEX));
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                new ArrayList<>(), payoutBlock, FOUNDER, FORK_INDEX));

        Map<String, Account> balances = new HashMap<>();
        assertTrue(UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                balances, fixture.chain, payoutBlock, FOUNDER, FORK_INDEX));
        assertEquals(0, fixture.amount.compareTo(balances.get(fixture.winner).getDigitalDollarBalance()));
    }

    @Test
    public void wrongImmatureFounderSenderAndDuplicatePayoutsAreRejectedOrNotCredited() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        List<DtoTransaction> payouts = Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX);
        DtoTransaction validPayout = payouts.get(0);

        Block wrongWinnerBlock = block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex),
                List.of(payout(SENDER_ADDRESS, validPayout.getDigitalDollar())));
        Block wrongAmountBlock = block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex),
                List.of(payout(fixture.winner, validPayout.getDigitalDollar() + 0.01)));
        Block immatureBlock = block(fixture.payoutIndex - 1L, PAYOUT_MINER, hash(fixture.payoutIndex - 1L),
                List.of(validPayout));
        Block founderBlock = block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex),
                List.of(payout(FOUNDER, validPayout.getDigitalDollar())));
        Block duplicateBlock = block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex),
                List.of(validPayout, payout(fixture.winner, validPayout.getDigitalDollar())));

        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, wrongWinnerBlock, FOUNDER, FORK_INDEX));
        assertFalse(UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                new HashMap<>(), fixture.chain, wrongWinnerBlock, FOUNDER, FORK_INDEX));

        assertFalse(UtilsBlock.validateV35JackpotPayoutWithChainContext(
                fixture.chain, wrongAmountBlock, wrongAmountBlock.getDtoTransactions().get(0), FOUNDER, FORK_INDEX).isValid());
        assertFalse(UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                new HashMap<>(), fixture.chain, wrongAmountBlock, FOUNDER, FORK_INDEX));

        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, immatureBlock, FOUNDER, FORK_INDEX));
        assertFalse(UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                new HashMap<>(), fixture.chain, immatureBlock, FOUNDER, FORK_INDEX));

        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, founderBlock, FOUNDER, FORK_INDEX));
        assertFalse(UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                new HashMap<>(), fixture.chain, founderBlock, FOUNDER, FORK_INDEX));

        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, duplicateBlock, FOUNDER, FORK_INDEX));
        assertNotEquals(SENDER_ADDRESS, fixture.winner);
        assertTrue(Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex + 1L, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX).isEmpty());
    }

    @Test
    public void dailyAndWeeklyPayoutsCanBeMinedValidatedAndAccountedTogether() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.WEEKLY);
        List<DtoTransaction> payouts = Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX);
        Block payoutBlock = block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex), payouts);
        Map<JackpotCalculator.JackpotPeriod, BigDecimal> periods = new EnumMap<>(JackpotCalculator.JackpotPeriod.class);

        for (DtoTransaction payout : payouts) {
            JackpotCalculator.JackpotPeriod period = JackpotCalculator.classifyPayoutAmount(payout.getDigitalDollar());
            periods.put(period, BigDecimal.valueOf(payout.getDigitalDollar()));
        }

        assertEquals(2, payouts.size());
        assertEquals(0, JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.DAILY)
                .compareTo(periods.get(JackpotCalculator.JackpotPeriod.DAILY)));
        assertEquals(0, JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.WEEKLY)
                .compareTo(periods.get(JackpotCalculator.JackpotPeriod.WEEKLY)));
        assertTrue(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain, payoutBlock, FOUNDER, FORK_INDEX));

        Map<String, BigDecimal> expectedByWinner = new HashMap<>();
        for (DtoTransaction payout : payouts) {
            expectedByWinner.merge(
                    payout.getCustomer(),
                    BigDecimal.valueOf(payout.getDigitalDollar()),
                    BigDecimal::add);
        }
        Map<String, Account> balances = new HashMap<>();
        assertTrue(UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                balances, fixture.chain, payoutBlock, FOUNDER, FORK_INDEX));
        for (Map.Entry<String, BigDecimal> entry : expectedByWinner.entrySet()) {
            assertEquals(0, entry.getValue().compareTo(balances.get(entry.getKey()).getDigitalDollarBalance()));
        }

        List<DtoTransaction> duplicateDaily = new ArrayList<>(payouts);
        duplicateDaily.add(payouts.get(0));
        assertFalse(UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(
                fixture.chain,
                block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex), duplicateDaily),
                FOUNDER,
                FORK_INDEX));
    }

    private static PrivateKey basisPrivateKey() throws Exception {
        Base base = new Base58();
        return UtilsSecurity.privateBytToPrivateKey(base.decode(Seting.BASIS_PASSWORD));
    }

    private static Fixture fixture(JackpotCalculator.JackpotPeriod period) {
        long window = JackpotCalculator.windowBlocksForPeriod(period.getPeriodName());
        long periodEnd = FORK_INDEX + window - 1L;
        long payoutIndex = periodEnd + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L;
        JackpotPeriodInstance instance = V35JackpotChainContextAdapter.buildPeriodInstance(
                period, payoutIndex, FORK_INDEX);
        List<Block> chain = chain(FORK_INDEX, instance.getMaturityBlock());
        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(),
                instance.getPeriodEnd(),
                V35JackpotChainContextAdapter.collectEligibleTickets(chain, instance, FORK_INDEX),
                V35JackpotChainContextAdapter.collectFutureHashes(chain, instance));
        return new Fixture(chain, instance, payoutIndex,
                JackpotCalculator.expectedPayoutAmount(period), proof.getWinnerMinerAddress());
    }

    private static List<Block> chain(long from, long to) {
        List<Block> blocks = new ArrayList<>();
        for (long i = from; i <= to; i++) {
            blocks.add(block(i, "miner-" + i, hash(i), new ArrayList<>()));
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
        private final JackpotPeriodInstance instance;
        private final long payoutIndex;
        private final BigDecimal amount;
        private final String winner;

        private Fixture(List<Block> chain,
                        JackpotPeriodInstance instance,
                        long payoutIndex,
                        BigDecimal amount,
                        String winner) {
            this.chain = chain;
            this.instance = instance;
            this.payoutIndex = payoutIndex;
            this.amount = amount;
            this.winner = winner;
        }
    }
}
