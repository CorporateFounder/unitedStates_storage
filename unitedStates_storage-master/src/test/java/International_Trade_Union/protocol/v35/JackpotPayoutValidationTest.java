package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JackpotPayoutValidationTest {
    private static final String FOUNDER = "founder-address";

    @Test
    public void validJackpotProofIsAcceptedByHelperWhenV35GateIsSimulatedActive() {
        JackpotWinnerProof proof = dailyProof();
        int ticketCount = 3;
        double amount = JackpotCalculator.expectedPayoutAmount(JackpotCalculator.PERIOD_DAILY, ticketCount);

        assertTrue(JackpotCalculator.isValidJackpotPayout(true, proof.getMaturityBlock(),
                JackpotCalculator.PERIOD_DAILY, amount, proof.getWinnerMinerAddress(),
                FOUNDER, proof, ticketCount, false));
    }

    @Test
    public void currentDisabledForkGateRejectsJackpotPayout() {
        JackpotWinnerProof proof = dailyProof();
        int ticketCount = 3;

        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertFalse(JackpotCalculator.isV35JackpotPayoutAllowedAt(proof.getMaturityBlock()));
        assertFalse(JackpotCalculator.isValidJackpotPayout(proof.getMaturityBlock(),
                JackpotCalculator.PERIOD_DAILY,
                JackpotCalculator.expectedPayoutAmount(JackpotCalculator.PERIOD_DAILY, ticketCount),
                proof.getWinnerMinerAddress(), FOUNDER, proof, ticketCount, false));
    }

    @Test
    public void wrongWinnerWrongAmountAndImmaturePayoutAreRejected() {
        JackpotWinnerProof proof = dailyProof();
        int ticketCount = 3;
        double amount = JackpotCalculator.expectedPayoutAmount(JackpotCalculator.PERIOD_DAILY, ticketCount);

        assertFalse(JackpotCalculator.isValidJackpotPayout(true, proof.getMaturityBlock(),
                JackpotCalculator.PERIOD_DAILY, amount, "wrong-miner", FOUNDER, proof, ticketCount, false));
        assertFalse(JackpotCalculator.isValidJackpotPayout(true, proof.getMaturityBlock(),
                JackpotCalculator.PERIOD_DAILY, amount + 0.01, proof.getWinnerMinerAddress(), FOUNDER, proof, ticketCount, false));
        assertFalse(JackpotCalculator.isValidJackpotPayout(true, proof.getMaturityBlock() - 1,
                JackpotCalculator.PERIOD_DAILY, amount, proof.getWinnerMinerAddress(), FOUNDER, proof, ticketCount, false));
    }

    @Test
    public void duplicateSenderBasedFounderAndInvalidPayoutsAreRejected() {
        JackpotWinnerProof proof = dailyProof();
        int ticketCount = 3;
        double amount = JackpotCalculator.expectedPayoutAmount(JackpotCalculator.PERIOD_DAILY, ticketCount);

        assertFalse(JackpotCalculator.isValidJackpotPayout(true, proof.getMaturityBlock(),
                JackpotCalculator.PERIOD_DAILY, amount, proof.getWinnerMinerAddress(), FOUNDER, proof, ticketCount, true));
        assertFalse(JackpotCalculator.isValidJackpotPayout(true, proof.getMaturityBlock(),
                JackpotCalculator.PERIOD_DAILY, amount, "transaction-sender", FOUNDER, proof, ticketCount, false));
        assertFalse(JackpotCalculator.isValidJackpotPayout(true, proof.getMaturityBlock(),
                JackpotCalculator.PERIOD_DAILY, amount, FOUNDER, FOUNDER, proof, ticketCount, false));
        assertFalse(JackpotCalculator.isValidJackpotPayout(true, proof.getMaturityBlock(),
                "unknown", amount, proof.getWinnerMinerAddress(), FOUNDER, proof, ticketCount, false));
    }

    @Test
    public void minerInstantRewardRemainsSixGold() {
        assertEquals(6.00, V35RewardCalculator.getInstantMinerGoldReward(), 0.00000001);
        assertTrue(V35RewardCalculator.isValidInstantMinerReward(6.00, 0.00));
        assertFalse(V35RewardCalculator.isValidInstantMinerReward(6.01, 0.00));
    }

    private static JackpotWinnerProof dailyProof() {
        return JackpotCalculator.selectWinner(0L, 431L,
                Arrays.asList(
                        JackpotTicket.of(1L, "miner-a"),
                        JackpotTicket.of(2L, "miner-b"),
                        JackpotTicket.of(3L, "miner-c")),
                futureHashes(60));
    }

    private static List<String> futureHashes(int count) {
        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            hashes.add(String.format("%064x", i + 1));
        }
        return hashes;
    }
}
