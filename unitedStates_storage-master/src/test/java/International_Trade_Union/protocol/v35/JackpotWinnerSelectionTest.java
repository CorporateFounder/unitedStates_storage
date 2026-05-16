package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JackpotWinnerSelectionTest {

    @Test
    public void eligibilityRequiresV35BlockAndMinerAddress() {
        assertTrue(JackpotCalculator.isEligibleTicket(10L, "miner", true));
        assertFalse(JackpotCalculator.isEligibleTicket(10L, "miner", false));
        assertFalse(JackpotCalculator.isEligibleTicket(10L, "", true));
        assertFalse(JackpotCalculator.isEligibleTicket(10L, null, true));
        assertFalse(JackpotCalculator.isEligibleTicket(-1L, "miner", true));
    }

    @Test
    public void jackpotTicketSanitizesAndCapsWeight() {
        JackpotTicket ticket = new JackpotTicket(7L, " miner ", 999.0);

        assertEquals(7L, ticket.getBlockIndex());
        assertEquals("miner", ticket.getMinerAddress());
        assertEquals(TicketWeightCalculator.maxWeight(), ticket.getWeight(), 0.00000001);
        assertTrue(ticket.isValid());
        assertFalse(new JackpotTicket(7L, "miner", -1.0).isValid());
    }

    @Test
    public void maturityRequiresSixtyFutureBlocks() {
        long periodEnd = 575L;

        assertEquals(635L, JackpotCalculator.maturityBlock(periodEnd, JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS));
        assertFalse(JackpotCalculator.isMature(634L, periodEnd));
        assertTrue(JackpotCalculator.isMature(635L, periodEnd));
    }

    @Test
    public void seedFromFutureHashesRequiresDelayAndIsOrderSensitive() {
        List<String> hashes = futureHashes(60);
        List<String> same = futureHashes(60);
        List<String> reversed = new ArrayList<>(hashes);
        Collections.reverse(reversed);

        String seed = JackpotCalculator.seedFromFutureHashes(hashes);

        assertEquals(seed, JackpotCalculator.seedFromFutureHashes(same));
        assertNotEquals(seed, JackpotCalculator.seedFromFutureHashes(reversed));
        assertEquals(64, seed.length());
        assertEquals("", JackpotCalculator.seedFromFutureHashes(futureHashes(59)));
        assertEquals("", JackpotCalculator.seedFromFutureHashes(Arrays.asList(null, hex(1))));
    }

    @Test
    public void eligibleTicketsAreSortedByBlockIndex() {
        List<JackpotTicket> tickets = Arrays.asList(
                JackpotTicket.of(30L, "miner-30"),
                JackpotTicket.of(10L, "miner-10"),
                new JackpotTicket(-1L, "bad", 1.0),
                JackpotTicket.of(20L, "miner-20")
        );

        List<JackpotTicket> eligible = JackpotCalculator.eligibleTickets(tickets);

        assertEquals(3, eligible.size());
        assertEquals(10L, eligible.get(0).getBlockIndex());
        assertEquals(20L, eligible.get(1).getBlockIndex());
        assertEquals(30L, eligible.get(2).getBlockIndex());
    }

    @Test
    public void winnerSelectionIsDeterministicAndReturnsProof() {
        List<JackpotTicket> tickets = Arrays.asList(
                JackpotTicket.of(900L, "outside-period"),
                JackpotTicket.of(102L, "miner-c"),
                JackpotTicket.of(100L, "miner-a"),
                JackpotTicket.of(101L, "miner-b")
        );
        List<String> hashes = futureHashes(60);

        JackpotWinnerProof first = JackpotCalculator.selectWinner(0L, 575L, tickets, hashes);
        JackpotWinnerProof second = JackpotCalculator.selectWinner(0L, 575L, tickets, hashes);

        assertEquals(first, second);
        assertTrue(first.hasWinner());
        assertEquals(0L, first.getPeriodStart());
        assertEquals(575L, first.getPeriodEnd());
        assertEquals(635L, first.getMaturityBlock());
        assertEquals(60, first.getRandomDelayBlocks());
        assertTrue(first.getSelectedOffset() >= 0);
        assertTrue(first.getSelectedOffset() < 3);
        assertNotEquals("outside-period", first.getWinnerMinerAddress());
        assertEquals(JackpotCalculator.eligibleTickets(tickets.subList(1, 4)).get(first.getSelectedOffset()).getBlockIndex(),
                first.getSelectedBlockIndex());
        assertEquals(JackpotCalculator.eligibleTickets(tickets.subList(1, 4)).get(first.getSelectedOffset()).getMinerAddress(),
                first.getWinnerMinerAddress());
    }

    @Test
    public void winnerSelectionCanRunWithoutPeriodProofFields() {
        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                Arrays.asList(JackpotTicket.of(2L, "miner-b"), JackpotTicket.of(1L, "miner-a")),
                futureHashes(60));

        assertTrue(proof.hasWinner());
        assertEquals(-1L, proof.getPeriodStart());
        assertEquals(-1L, proof.getPeriodEnd());
        assertEquals(-1L, proof.getMaturityBlock());
    }

    @Test
    public void winnerSelectionRejectsEmptyOrImmatureInputs() {
        assertFalse(JackpotCalculator.selectWinner(0L, 575L, Collections.emptyList(), futureHashes(60)).hasWinner());
        assertFalse(JackpotCalculator.selectWinner(0L, 575L,
                Collections.singletonList(JackpotTicket.of(1L, "miner")), futureHashes(59)).hasWinner());
        assertFalse(JackpotCalculator.selectWinner(10L, 0L,
                Collections.singletonList(JackpotTicket.of(1L, "miner")), futureHashes(60)).hasWinner());
    }

    @Test
    public void dailyAndWeeklyPeriodHelpersSupportWinnerSelection() {
        assertEquals(0L, JackpotCalculator.periodStartForBlock(575L, JackpotCalculator.dailyWindowBlocks()));
        assertEquals(575L, JackpotCalculator.periodEndForBlock(575L, JackpotCalculator.dailyWindowBlocks()));
        assertEquals(635L, JackpotCalculator.maturityBlockForPeriod(575L, JackpotCalculator.dailyWindowBlocks()));
        assertEquals(4031L, JackpotCalculator.periodEndForBlock(1L, JackpotCalculator.weeklyWindowBlocks()));

        JackpotWinnerProof proof = JackpotCalculator.selectWinner(0L, 4031L,
                Arrays.asList(JackpotTicket.of(5L, "daily-miner"), JackpotTicket.of(40L, "weekly-miner")),
                futureHashes(60));

        assertTrue(proof.hasWinner());
        assertTrue(proof.getWinnerMinerAddress().equals("daily-miner")
                || proof.getWinnerMinerAddress().equals("weekly-miner"));
    }

    private static List<String> futureHashes(int count) {
        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            hashes.add(hex(i + 1));
        }
        return hashes;
    }

    private static String hex(int value) {
        return String.format("%064x", value);
    }
}
