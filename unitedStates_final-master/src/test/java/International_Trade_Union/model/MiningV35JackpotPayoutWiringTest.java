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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MiningV35JackpotPayoutWiringTest {
    private static final long FORK_INDEX = 5_000L;
    private static final String FOUNDER = "founder-address";
    private static final String PAYOUT_MINER = "payout-miner";

    @Test
    public void disabledForkReturnsNoMiningJackpotPayouts() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertTrue(Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey()).isEmpty());
    }

    @Test
    public void matureDailyPayoutIsSignedAndProofValid() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);

        List<DtoTransaction> payouts = Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX);

        assertEquals(1, payouts.size());
        DtoTransaction payout = payouts.get(0);
        assertNotNull(payout.getSign());
        assertEquals(Seting.BASIS_ADDRESS, payout.getSender());
        assertEquals(fixture.winner, payout.getCustomer());
        assertEquals(0.0, payout.getDigitalStockBalance());
        assertEquals(0, BigDecimal.valueOf(payout.getDigitalDollar()).compareTo(fixture.amount));
        assertFalse(FOUNDER.equals(payout.getCustomer()));
        assertFalse(JackpotCalculator.isCanonicalPayoutAmount(V35RewardCalculator.getInstantMinerGoldReward()));

        JackpotProofResult result = V35JackpotChainContextAdapter.verifyPayoutWithChainContext(
                fixture.chain,
                block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex), payouts),
                payout,
                FOUNDER,
                FORK_INDEX);
        assertTrue(result.isValid(), result.getReason());
    }

    @Test
    public void multipleMaturedPeriodsCanProduceMultipleSignedPayouts() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.WEEKLY);

        List<DtoTransaction> payouts = Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX);
        Set<JackpotCalculator.JackpotPeriod> periods = EnumSet.noneOf(JackpotCalculator.JackpotPeriod.class);
        for (DtoTransaction payout : payouts) {
            assertNotNull(payout.getSign());
            periods.add(JackpotCalculator.classifyPayoutAmount(payout.getDigitalDollar()));
        }

        assertEquals(2, payouts.size());
        assertTrue(periods.contains(JackpotCalculator.JackpotPeriod.DAILY));
        assertTrue(periods.contains(JackpotCalculator.JackpotPeriod.WEEKLY));
    }

    @Test
    public void duplicateFounderAndImmatureContextsAreSkipped() throws Exception {
        Fixture fixture = fixture(JackpotCalculator.JackpotPeriod.DAILY);
        List<Block> duplicatePriorPayout = new ArrayList<>(fixture.chain);
        duplicatePriorPayout.add(block(fixture.payoutIndex, PAYOUT_MINER, hash(fixture.payoutIndex),
                Mining.buildV35JackpotPayoutTransactionsForMining(
                        fixture.chain, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX)));
        List<Block> founderWins = chain(FORK_INDEX, fixture.instance.getMaturityBlock(), FOUNDER);

        assertTrue(Mining.buildV35JackpotPayoutTransactionsForMining(
                fixture.chain, fixture.payoutIndex - 1L, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX).isEmpty());
        assertTrue(Mining.buildV35JackpotPayoutTransactionsForMining(
                duplicatePriorPayout, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX).isEmpty());
        assertTrue(Mining.buildV35JackpotPayoutTransactionsForMining(
                founderWins, fixture.payoutIndex, PAYOUT_MINER, FOUNDER, basisPrivateKey(), FORK_INDEX).isEmpty());
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
        List<Block> chain = chain(FORK_INDEX, instance.getMaturityBlock(), "");
        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(),
                instance.getPeriodEnd(),
                V35JackpotChainContextAdapter.collectEligibleTickets(chain, instance, FORK_INDEX),
                V35JackpotChainContextAdapter.collectFutureHashes(chain, instance));
        return new Fixture(chain, instance, payoutIndex,
                JackpotCalculator.expectedPayoutAmount(period), proof.getWinnerMinerAddress());
    }

    private static List<Block> chain(long from, long to, String minerAddress) {
        List<Block> blocks = new ArrayList<>();
        for (long i = from; i <= to; i++) {
            String miner = minerAddress == null || minerAddress.isEmpty() ? "miner-" + i : minerAddress;
            blocks.add(block(i, miner, hash(i), new ArrayList<>()));
        }
        return blocks;
    }

    private static Block block(long index, String minerAddress, String blockHash, List<DtoTransaction> transactions) {
        return new Block(new ArrayList<>(transactions), "previous-" + index, minerAddress, FOUNDER,
                0L, 0.0, 1L, new Timestamp(index * 1000L), index, blockHash);
    }

    private static String hash(long index) {
        return String.format("%064x", index + 1L);
    }

    @SuppressWarnings("unused")
    private static DtoTransaction payout(String recipient, double amount) {
        return new DtoTransaction(Seting.BASIS_ADDRESS, recipient, amount, 0.0, new Laws(), 0.0, VoteEnum.YES);
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
