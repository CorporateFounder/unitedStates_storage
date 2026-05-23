package International_Trade_Union.controllers;

import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.protocol.v35.V35SupplyApiResponseBuilder;
import International_Trade_Union.protocol.v35.V35SupplyCalculator;
import International_Trade_Union.protocol.v35.V35SupplySnapshot;
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

    @Test
    public void founderBurnSnapshotEndpointIsReadOnlyAndReportsConfiguredBurnAddress() throws IOException {
        FakeBlockService blockService = new FakeBlockService();
        V35SupplyController controller = new V35SupplyController(blockService);

        Map<String, Object> response = controller.founderBurnSnapshot();

        assertEquals(Seting.ADDRESS_FOUNDER, response.get("founderAddress"));
        assertEquals("BURN_ADDRESS", response.get("burnAddress"));
        assertEquals(true, response.get("burnAddressConfigured"));
        assertEquals(false, response.get("isBurnAddressPlaceholder"));
        assertEquals(-1L, response.get("snapshotHeight"));
        assertEquals("UNKNOWN", response.get("snapshotBlockHash"));
        assertEquals("100.00", response.get("founderBalanceGold"));
        assertEquals("0.00", response.get("actualBurnedGold"));
        assertEquals("1261440.00", response.get("annualEmissionGold"));
        assertEquals(false, response.get("v35Active"));
        assertEquals("Long.MAX_VALUE", response.get("v35ForkIndex"));
        assertEquals(80, response.get("recommendedBurnPercent"));
        assertEquals(false, response.get("burnTransactionCreated"));
        assertFalse(blockService.mutated);
    }

    @Test
    public void founderBurnSnapshotBuilderReturnsAllOptionsAndRecommendedProofChecklist() {
        V35SupplySnapshot snapshot = V35SupplyCalculator.snapshot(
                new BigDecimal("1000.00"),
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                BigDecimal.ZERO);

        Map<String, Object> response = V35SupplyApiResponseBuilder.founderBurnSnapshot(snapshot, 42L, "hash42");

        assertEquals(42L, response.get("snapshotHeight"));
        assertEquals("hash42", response.get("snapshotBlockHash"));
        assertEquals("1000.00", response.get("totalMintedGold"));
        assertEquals("100.00", response.get("actualBurnedGold"));
        assertEquals("900.00", response.get("actualCirculatingGold"));

        List<Map<String, Object>> options = burnOptions(response);
        assertEquals(4, options.size());
        assertEquals(75, options.get(0).get("percent"));
        assertEquals(80, options.get(1).get("percent"));
        assertEquals(85, options.get(2).get("percent"));
        assertEquals(90, options.get(3).get("percent"));

        Map<String, Object> recommended = optionByPercent(options, 80);
        assertEquals(true, recommended.get("recommended"));
        assertEquals("80.00", recommended.get("burnGold"));
        assertEquals("20.00", recommended.get("founderReserveAfterBurnGold"));
        assertEquals("180.00", recommended.get("modeledBurnedGoldAfterBurn"));
        assertEquals("820.00", recommended.get("modeledCirculatingGoldAfterBurn"));
        assertEquals("153834.1463", recommended.get("modeledInflationAfterBurnPercent"));
        assertTrue(recommended.get("burnGold") instanceof String);

        Map<String, Object> proofChecklist = proofChecklist(response);
        assertEquals(Seting.ADDRESS_FOUNDER, proofChecklist.get("founderAddress"));
        assertEquals("BURN_ADDRESS", proofChecklist.get("burnAddress"));
        assertEquals(42L, proofChecklist.get("snapshotHeight"));
        assertEquals("hash42", proofChecklist.get("snapshotBlockHash"));
        assertEquals("100.00", proofChecklist.get("founderBalanceBeforeBurn"));
        assertEquals("80.00", proofChecklist.get("burnAmount"));
        assertEquals("PENDING_MANUAL_BURN", proofChecklist.get("burnTxHash"));
        assertEquals("PENDING_MANUAL_BURN", proofChecklist.get("burnBlockIndex"));
        assertEquals("PENDING_MANUAL_BURN", proofChecklist.get("burnBlockHash"));
        assertEquals("20.00", proofChecklist.get("founderBalanceAfterBurn"));
        assertEquals("180.00", proofChecklist.get("burnedSupply"));
        assertEquals("820.00", proofChecklist.get("circulatingSupply"));
        assertEquals("1261440.00", proofChecklist.get("annualEmission"));
        assertEquals("153834.1463", proofChecklist.get("inflationAfterBurn"));
    }

    @Test
    public void founderBurnSnapshotSeparatesModeledValuesFromActualSupply() {
        V35SupplySnapshot snapshot = V35SupplyCalculator.snapshot(
                new BigDecimal("159550000.00"),
                BigDecimal.ZERO,
                new BigDecimal("76030000.00"),
                BigDecimal.ZERO);

        Map<String, Object> response = V35SupplyApiResponseBuilder.founderBurnSnapshot(snapshot, 100L, "snapshotHash");
        Map<String, Object> recommended = optionByPercent(burnOptions(response), 80);

        assertEquals("0.00", response.get("actualBurnedGold"));
        assertEquals("159550000.00", response.get("actualCirculatingGold"));
        assertEquals("60824000.00", recommended.get("burnGold"));
        assertEquals("15206000.00", recommended.get("founderReserveAfterBurnGold"));
        assertEquals("60824000.00", recommended.get("modeledBurnedGoldAfterBurn"));
        assertEquals("98726000.00", recommended.get("modeledCirculatingGoldAfterBurn"));
        assertEquals("1.2777", recommended.get("modeledInflationAfterBurnPercent"));
        assertEquals(false, response.get("burnTransactionCreated"));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> burnOptions(Map<String, Object> response) {
        return (List<Map<String, Object>>) response.get("burnOptions");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> proofChecklist(Map<String, Object> response) {
        return (Map<String, Object>) response.get("proofChecklist");
    }

    private static Map<String, Object> optionByPercent(List<Map<String, Object>> options, int percent) {
        for (Map<String, Object> option : options) {
            if (option.get("percent").equals(percent)) {
                return option;
            }
        }
        throw new AssertionError("missing burn option " + percent);
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
