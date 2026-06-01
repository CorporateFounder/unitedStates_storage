package International_Trade_Union.controllers;

import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.protocol.v35.JackpotCalculator;
import International_Trade_Union.protocol.v35.V35JackpotApiResponseBuilder;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35JackpotControllerTest {
    @Test
    public void statusRendersDisabledStateAndPolicyValues() throws IOException {
        FakeBlockService blockService = new FakeBlockService();
        V35JackpotController controller = new V35JackpotController(blockService);

        Map<String, Object> response = controller.status();

        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals(false, response.get("v35Active"));
        assertEquals(International_Trade_Union.protocol.v35.V35TestMode.configuredForkIndexValue(), response.get("v35ForkIndex"));
        assertEquals(200, response.get("blockTimeSeconds"));
        assertEquals(432L, response.get("blocksPerDay"));
        assertEquals("2.00", response.get("jackpotReservePerBlockGold"));
        assertEquals(60L, response.get("randomDelayBlocks"));
        assertEquals("BURN_ADDRESS", response.get("burnAddress"));
        assertEquals(true, response.get("isBurnAddressPlaceholder"));
        assertEquals(false, response.get("burnAddressConfigured"));
        assertEquals(false, response.get("publicBurnAddressPublished"));
        assertTrue(response.get("burnAddressWarning").toString().contains("not published yet"));
        assertEquals("minerAddress", response.get("winnerSource"));
        assertEquals(false, response.get("senderEligibilityAllowed"));
        assertEquals("V35 jackpot policy is configured but not active yet.", response.get("warning"));
        assertEquals(6, periods(response).size());
        assertEquals("DISABLED", periods(response).get(0).get("status"));
        assertFalse(blockService.mutated);
    }

    @Test
    public void periodsReturnsAllSixCanonicalDefinitions() {
        V35JackpotController controller = new V35JackpotController(new FakeBlockService());

        Map<String, Object> response = controller.periods();
        List<Map<String, Object>> periods = periods(response);

        assertEquals(6, periods.size());
        assertPeriod(periods.get(0), "DAILY", 432L, "302.40");
        assertPeriod(periods.get(1), "WEEKLY", 3024L, "1209.60");
        assertPeriod(periods.get(2), "MONTHLY", 12960L, "3888.00");
        assertPeriod(periods.get(3), "QUARTERLY", 38880L, "9720.00");
        assertPeriod(periods.get(4), "HALF_YEAR", 78624L, "15724.80");
        assertPeriod(periods.get(5), "YEARLY", 157680L, "23652.00");
        assertEquals(false, response.get("senderEligibilityAllowed"));
        assertEquals("minerAddress", response.get("winnerSource"));
        assertEquals(true, response.get("exactSlotOnly"));
        assertEquals(false, response.get("missedPayoutsPaidLater"));
    }

    @Test
    public void historyTicketsAndProofReturnSafeDisabledResponses() throws IOException {
        V35JackpotController controller = new V35JackpotController(new FakeBlockService());

        Map<String, Object> history = controller.history();
        Map<String, Object> tickets = controller.tickets("some-address");
        Map<String, Object> proof = controller.proof("DAILY", 431L);

        assertEquals("DISABLED", history.get("status"));
        assertTrue(((List<?>) history.get("items")).isEmpty());
        assertEquals(false, tickets.get("v35Active"));
        assertTrue(tickets.get("note").toString().contains("Only minerAddress tickets count"));
        assertTrue(tickets.get("note").toString().contains("Sender addresses are never jackpot participants"));
        assertEquals(0, periods(tickets).get(0).get("tickets"));
        assertEquals("DISABLED", proof.get("proofStatus"));
        assertEquals(false, proof.get("payoutPaid"));
    }

    @Test
    public void decimalFieldsAreStringSafeAndNoPayoutIsCreatedByBuilder() {
        Map<String, Object> response = V35JackpotApiResponseBuilder.periods(-1L);
        List<Map<String, Object>> periods = periods(response);

        assertTrue(periods.get(0).get("canonicalPayoutGold") instanceof String);
        assertEquals("302.40", periods.get(0).get("canonicalPayoutGold"));
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> periods(Map<String, Object> response) {
        return (List<Map<String, Object>>) response.get("periods");
    }

    private static void assertPeriod(Map<String, Object> row, String type, long window, String payout) {
        assertEquals(type, row.get("periodType"));
        assertEquals(window, row.get("windowBlocks"));
        assertEquals(payout, row.get("canonicalPayoutGold"));
        assertEquals(JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS, row.get("randomDelayBlocks"));
    }

    private static final class FakeBlockService extends BlockService {
        private boolean mutated;

        @Override
        public long countBlock() {
            return 0;
        }

        @Override
        public List<EntityBlock> findBySpecialIndexBetween(long from, long to) {
            return Collections.emptyList();
        }

        @Override
        public void saveBlock(EntityBlock entityBlock) throws IOException {
            mutated = true;
            throw new IOException("read-only endpoint must not save blocks");
        }
    }
}
