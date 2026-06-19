package International_Trade_Union.controllers;

import International_Trade_Union.protocol.v35.V35SupplyApiResponseBuilder;
import International_Trade_Union.protocol.v35.V35SupplyCalculator;
import International_Trade_Union.protocol.v35.V35SupplyContext;
import International_Trade_Union.protocol.v35.V35SupplySnapshot;
import International_Trade_Union.protocol.v35.V35SupplySnapshotProvider;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35SupplyControllerTest {
    @Test
    public void protocolExposesCanonicalV35Economics() {
        V35SupplyController controller = new V35SupplyController(new FakeSnapshotProvider());

        Map<String, Object> response = controller.protocol();

        assertEquals(false, response.get("v35Active"));
        assertEquals(514291L, response.get("v35ForkIndex"));
        assertEquals(200, response.get("blockTimeSeconds"));
        assertEquals(432L, response.get("blocksPerDay"));
        assertEquals("8.00", response.get("rewardPerBlockGold"));
        assertEquals("6.00", response.get("minerRewardGold"));
        assertEquals("2.00", response.get("jackpotReservePerBlockGold"));
        assertEquals("0.00", response.get("founderRewardGold"));
        assertEquals("BURN_ADDRESS", response.get("burnAddress"));
        assertEquals(true, response.get("isBurnAddressPlaceholder"));
        assertEquals(false, response.get("burnAddressConfigured"));
        assertEquals(false, response.get("publicBurnAddressPublished"));
    }

    @Test
    public void existingEndpointContractsUseSharedImmutableContext() {
        FakeSnapshotProvider provider = new FakeSnapshotProvider();
        V35SupplyController controller = new V35SupplyController(provider);

        Map<String, Object> supply = controller.supply();
        Map<String, Object> burn = controller.burn();
        Map<String, Object> founder = controller.founder();
        Map<String, Object> burnSnapshot = controller.founderBurnSnapshot();

        assertEquals("1000.00", supply.get("totalMintedGold"));
        assertEquals("0.00", supply.get("burnedGold"));
        assertEquals("100.00", founder.get("founderReserveGold"));
        assertEquals(false, burn.get("burnTransactionCreated"));
        assertEquals("hash42", burnSnapshot.get("snapshotBlockHash"));
        assertEquals("bounded_projection_test", supply.get("snapshotSource"));
        assertEquals(42L, supply.get("snapshotHeight"));
        assertEquals(4, provider.snapshotCalls.get());
    }

    @Test
    public void publicSnapshotUsesSameContextWithoutSecondCalculation() {
        FakeSnapshotProvider provider = new FakeSnapshotProvider();
        V35SupplyController controller = new V35SupplyController(provider);

        Map<String, Object> response = controller.publicSnapshot();

        assertEquals(1, provider.snapshotCalls.get());
        assertEquals("1000.00", response.get("totalMintedGold"));
        assertEquals("hash42", response.get("latestBlockHash"));
        assertEquals(42L, response.get("snapshotHeight"));
        assertEquals("bounded_projection_test", response.get("snapshotSource"));
        assertEquals(false, response.get("snapshotStale"));
        assertEquals("current", response.get("cacheState"));
        assertEquals(false, response.get("burnTransactionCreated"));
        assertEquals(false, response.get("founderBurnOccurred"));
    }

    @Test
    public void founderBurnSnapshotBuilderKeepsExistingProofContract() {
        V35SupplySnapshot snapshot = V35SupplyCalculator.snapshot(
                new BigDecimal("1000.00"),
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                BigDecimal.ZERO);

        Map<String, Object> response =
                V35SupplyApiResponseBuilder.founderBurnSnapshot(snapshot, 42L, "hash42");

        assertEquals(42L, response.get("snapshotHeight"));
        assertEquals("hash42", response.get("snapshotBlockHash"));
        assertEquals("1000.00", response.get("totalMintedGold"));
        assertEquals("100.00", response.get("actualBurnedGold"));
        assertEquals(4, burnOptions(response).size());
        assertEquals(false, response.get("burnTransactionCreated"));
    }

    @Test
    public void controllerSourceCannotHydrateFullChainOrAllAccounts() throws IOException {
        String source = Files.readString(Path.of(
                "src", "main", "java", "International_Trade_Union", "controllers",
                "V35SupplyController.java"));

        assertFalse(source.contains("BlockService"));
        assertFalse(source.contains("findBySpecialIndexBetween"));
        assertFalse(source.contains("findAllAccounts"));
        assertFalse(source.contains("EntityBlock"));
        assertFalse(source.contains("List<Block>"));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> burnOptions(Map<String, Object> response) {
        return (List<Map<String, Object>>) response.get("burnOptions");
    }

    private static final class FakeSnapshotProvider implements V35SupplySnapshotProvider {
        private final AtomicInteger snapshotCalls = new AtomicInteger();
        private final V35SupplyContext context;

        private FakeSnapshotProvider() {
            V35SupplySnapshot snapshot = V35SupplyCalculator.snapshot(
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("100.00"),
                    BigDecimal.ZERO,
                    false);
            this.context = new V35SupplyContext(
                    snapshot,
                    42L,
                    "hash42",
                    Instant.parse("2026-06-19T00:00:00Z"),
                    "bounded_projection_test",
                    false,
                    42L,
                    3L,
                    1);
        }

        @Override
        public V35SupplyContext snapshot() {
            snapshotCalls.incrementAndGet();
            return context;
        }

        @Override
        public long currentHeight() {
            return 42L;
        }

        @Override
        public Map<String, Object> diagnostics() {
            return Collections.singletonMap("supplyCachePresent", true);
        }
    }
}
