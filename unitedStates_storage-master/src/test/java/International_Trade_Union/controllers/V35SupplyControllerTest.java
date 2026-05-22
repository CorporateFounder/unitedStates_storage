package International_Trade_Union.controllers;

import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35SupplyControllerTest {
    @Test
    public void protocolExposesCanonicalV35EconomicsWhileDisabled() {
        V35SupplyController controller = new V35SupplyController(new FakeBlockService());

        Map<String, Object> response = controller.protocol();

        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals(false, response.get("v35Active"));
        assertEquals("Long.MAX_VALUE", response.get("v35ForkIndex"));
        assertEquals(200, response.get("blockTimeSeconds"));
        assertEquals(432L, response.get("blocksPerDay"));
        assertEquals("8.00", response.get("rewardPerBlockGold"));
        assertEquals("6.00", response.get("minerRewardGold"));
        assertEquals("2.00", response.get("jackpotReservePerBlockGold"));
        assertEquals("0.00", response.get("founderRewardGold"));
        assertEquals("BURN_ADDRESS", response.get("burnAddress"));
        assertEquals(false, response.get("isBurnAddressPlaceholder"));
        assertEquals(true, response.get("burnAddressConfigured"));
    }

    @Test
    public void supplyReportsConfiguredBurnAddressAndActualValues() throws IOException {
        FakeBlockService blockService = new FakeBlockService();
        V35SupplyController controller = new V35SupplyController(blockService);

        Map<String, Object> response = controller.supply();

        assertEquals("BURN_ADDRESS", response.get("burnAddress"));
        assertEquals(false, response.get("isBurnAddressPlaceholder"));
        assertEquals(true, response.get("burnAddressConfigured"));
        assertEquals("0.00", response.get("burnedGold"));
        assertEquals(response.get("totalMintedGold"), response.get("circulatingGold"));
        assertEquals("1261440.00", response.get("annualEmissionGold"));
        assertEquals(false, response.get("v35Active"));
        assertFalse(blockService.mutated);
    }

    @Test
    public void burnEndpointDoesNotPretendBurnTransactionExists() throws IOException {
        V35SupplyController controller = new V35SupplyController(new FakeBlockService());

        Map<String, Object> response = controller.burn();

        assertEquals(Seting.V35_BURN_ADDRESS, response.get("burnAddress"));
        assertEquals(false, response.get("isBurnAddressPlaceholder"));
        assertEquals(true, response.get("burnAddressConfigured"));
        assertEquals("0.00", response.get("burnedGold"));
        assertEquals(true, response.get("noSpendAfterV35"));
        assertEquals(false, response.get("burnTransactionCreated"));
    }

    @Test
    public void founderSeparatesModeledBurnFromActualBurnedSupply() throws IOException {
        V35SupplyController controller = new V35SupplyController(new FakeBlockService());

        Map<String, Object> response = controller.founder();

        assertEquals(Seting.ADDRESS_FOUNDER, response.get("founderAddress"));
        assertEquals("100.00", response.get("founderReserveGold"));
        assertEquals("0.00", response.get("founderRewardGold"));
        assertEquals("80", response.get("modeledBurnPercent"));
        assertEquals("80.00", response.get("modeledBurnGold"));
        assertEquals("0.00", response.get("actualBurnedGold"));
        assertEquals(true, response.get("modelOnly"));
    }

    @Test
    public void decimalFieldsAreStringSafeForJsonPrecision() throws IOException {
        V35SupplyController controller = new V35SupplyController(new FakeBlockService());

        Map<String, Object> response = controller.supply();

        assertTrue(response.get("totalMintedGold") instanceof String);
        assertTrue(response.get("burnedGold") instanceof String);
        assertTrue(response.get("circulatingGold") instanceof String);
        assertTrue(response.get("annualEmissionGold") instanceof String);
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
        public List<EntityAccount> findAllAccounts() {
            return Collections.singletonList(
                    new EntityAccount(Seting.ADDRESS_FOUNDER, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO));
        }

        @Override
        public void saveBlock(EntityBlock entityBlock) throws IOException {
            mutated = true;
            throw new IOException("read-only endpoint must not save blocks");
        }

        @Override
        public void saveAccount(EntityAccount entityAccount) throws IOException {
            mutated = true;
            throw new IOException("read-only endpoint must not save accounts");
        }
    }
}
