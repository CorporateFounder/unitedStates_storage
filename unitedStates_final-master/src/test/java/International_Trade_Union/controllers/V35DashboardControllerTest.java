package International_Trade_Union.controllers;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35DashboardControllerTest {
    @Test
    public void dashboardRouteRendersWhileV35Disabled() {
        V35DashboardController controller = controllerWithUnavailableLiveData();
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.dashboard(model);

        assertEquals("v35-dashboard", view);
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals(false, model.get("v35Active"));
        assertEquals("Long.MAX_VALUE", model.get("v35ForkIndex"));
        assertEquals("BURN_ADDRESS", model.get("burnAddress"));
        assertEquals(true, model.get("burnAddressConfigured"));
        assertEquals(false, model.get("isBurnAddressPlaceholder"));
        assertEquals(false, model.get("liveProtocolAvailable"));
        assertEquals(false, model.get("liveSupplyAvailable"));
    }

    @Test
    public void dashboardShowsCanonicalV35EconomicsAndUnavailableLiveSupply() {
        V35DashboardController controller = controllerWithUnavailableLiveData();
        ExtendedModelMap model = new ExtendedModelMap();

        controller.dashboard(model);

        assertEquals(200, model.get("blockTimeSeconds"));
        assertEquals(432L, model.get("blocksPerDay"));
        assertEquals("8.00", model.get("rewardPerBlockGold"));
        assertEquals("6.00", model.get("minerRewardGold"));
        assertEquals("2.00", model.get("jackpotReservePerBlockGold"));
        assertEquals("0.00", model.get("founderRewardGold"));
        assertEquals("1261440.00", model.get("annualEmissionGold"));
        assertEquals(false, model.get("liveSupplyAvailable"));
        assertEquals("Unavailable", model.get("totalMintedGold"));
        assertEquals("Unavailable", model.get("burnedGold"));
        assertEquals("Unavailable", model.get("circulatingGold"));
        assertTrue(String.valueOf(model.get("liveApiError")).contains("Live V35 data unavailable"));
    }

    @Test
    public void dashboardContainsBurnOptionsAndJackpotPolicy() {
        V35DashboardController controller = controllerWithUnavailableLiveData();
        ExtendedModelMap model = new ExtendedModelMap();

        controller.dashboard(model);

        List<Map<String, Object>> burnOptions = burnOptions(model);
        assertEquals(4, burnOptions.size());
        assertEquals(75, burnOptions.get(0).get("percent"));
        assertEquals(80, burnOptions.get(1).get("percent"));
        assertEquals(true, burnOptions.get(1).get("recommended"));
        assertEquals("Unavailable", burnOptions.get(1).get("burnGold"));
        assertEquals(90, burnOptions.get(3).get("percent"));

        List<Map<String, String>> jackpotPeriods = jackpotPeriods(model);
        assertEquals(6, jackpotPeriods.size());
        assertEquals("daily", jackpotPeriods.get(0).get("name"));
        assertEquals("432", jackpotPeriods.get(0).get("windowBlocks"));
        assertEquals("302.40", jackpotPeriods.get(0).get("payoutGold"));
        assertEquals("yearly", jackpotPeriods.get(5).get("name"));
        assertEquals("157680", jackpotPeriods.get(5).get("windowBlocks"));
        assertEquals("23652.00", jackpotPeriods.get(5).get("payoutGold"));
    }

    @Test
    public void dashboardOverlaysLiveReadOnlyValuesWhenEndpointsAreAvailable() {
        V35DashboardController controller = new V35DashboardController(clientReturning(liveData()));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.dashboard(model);

        assertEquals(true, model.get("liveProtocolAvailable"));
        assertEquals(true, model.get("liveSupplyAvailable"));
        assertEquals(true, model.get("liveBurnAvailable"));
        assertEquals(true, model.get("liveFounderSnapshotAvailable"));
        assertEquals("http://node.test", model.get("liveApiBaseUrl"));
        assertEquals(false, model.get("v35Active"));
        assertEquals("Long.MAX_VALUE", model.get("v35ForkIndex"));
        assertEquals("159550000.00", model.get("totalMintedGold"));
        assertEquals("123.45", model.get("burnedGold"));
        assertEquals("159549876.55", model.get("circulatingGold"));
        assertEquals("76030000.00", model.get("founderReserveGold"));
        assertEquals("302.40", model.get("jackpotPaidGold"));
        assertEquals("500", model.get("snapshotHeight"));
        assertEquals("abc123", model.get("snapshotBlockHash"));
        assertEquals("76030000.00", model.get("founderBalanceGold"));
        assertEquals(false, model.get("burnTransactionCreated"));

        List<Map<String, Object>> options = burnOptions(model);
        assertEquals("57022500.00", options.get(0).get("burnGold"));
        assertEquals(true, options.get(1).get("recommended"));
        assertEquals("60824000.00", options.get(1).get("burnGold"));

        List<String> checklist = proofChecklist(model);
        assertTrue(checklist.stream().anyMatch(value -> value.contains("burnTxHash: PENDING_MANUAL_BURN")));
    }

    @Test
    public void modeledBurnSnapshotDoesNotOverwriteActualSupplyValues() {
        V35DashboardController controller = new V35DashboardController(clientReturning(liveData()));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.dashboard(model);

        assertEquals("123.45", model.get("burnedGold"));
        assertEquals("159549876.55", model.get("circulatingGold"));
        assertTrue(burnOptions(model).stream()
                .anyMatch(option -> "60824123.45".equals(option.get("modeledBurnedGoldAfterBurn"))));
    }

    @Test
    public void dashboardTemplateContainsSafetyWordingAndHeaderLink() throws Exception {
        String template = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/v35-dashboard.html")),
                StandardCharsets.UTF_8);
        String header = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/blocks/header.html")),
                StandardCharsets.UTF_8);

        assertTrue(template.contains("V35 is not active while V35_RESCUE_FORK_INDEX is Long.MAX_VALUE."));
        assertTrue(template.contains("Modeled burn values are projections."));
        assertTrue(template.contains("Actual supply and burn"));
        assertTrue(template.contains("Live read-only data"));
        assertTrue(template.contains("Live V35 data unavailable"));
        assertTrue(template.contains("BURN_ADDRESS is a protocol sink"));
        assertTrue(template.contains("Manual difficulty is legacy-only after V35."));
        assertTrue(template.contains("No last winners are shown here until a read-only jackpot status/history endpoint exists."));
        assertTrue(header.contains("/v35-dashboard"));
        assertTrue(header.contains("V35 dashboard"));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> burnOptions(ExtendedModelMap model) {
        return (List<Map<String, Object>>) model.get("burnOptions");
    }

    @SuppressWarnings("unchecked")
    private static List<String> proofChecklist(ExtendedModelMap model) {
        return (List<String>) model.get("proofChecklist");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, String>> jackpotPeriods(ExtendedModelMap model) {
        return (List<Map<String, String>>) model.get("jackpotPeriods");
    }

    private static V35DashboardController controllerWithUnavailableLiveData() {
        return new V35DashboardController(clientReturning(
                V35DashboardApiClient.LiveData.unavailable("http://node.test", "offline")));
    }

    private static V35DashboardApiClient clientReturning(V35DashboardApiClient.LiveData data) {
        return new V35DashboardApiClient() {
            @Override
            public LiveData fetch() {
                return data;
            }
        };
    }

    private static V35DashboardApiClient.LiveData liveData() {
        Map<String, Object> protocol = map(
                "v35Active", false,
                "v35ForkIndex", "Long.MAX_VALUE",
                "blockTimeSeconds", 200,
                "blocksPerDay", 432,
                "rewardPerBlockGold", "8.00",
                "minerRewardGold", "6.00",
                "jackpotReservePerBlockGold", "2.00",
                "founderRewardGold", "0.00",
                "burnAddress", "BURN_ADDRESS",
                "founderAddress", Seting.ADDRESS_FOUNDER,
                "burnAddressConfigured", true,
                "isBurnAddressPlaceholder", false,
                "annualEmissionGold", "1261440.00"
        );
        Map<String, Object> supply = map(
                "totalMintedGold", "159550000.00",
                "burnedGold", "123.45",
                "circulatingGold", "159549876.55",
                "founderReserveGold", "76030000.00",
                "jackpotPaidGold", "302.40",
                "annualEmissionGold", "1261440.00",
                "valid", true,
                "invalidReason", "",
                "snapshotSource", "canonical_chain_and_account_map"
        );
        Map<String, Object> burn = map(
                "burnAddress", "BURN_ADDRESS",
                "burnAddressConfigured", true,
                "isBurnAddressPlaceholder", false,
                "burnedGold", "123.45",
                "burnTransactionCreated", false
        );
        Map<String, Object> founder = map(
                "founderAddress", Seting.ADDRESS_FOUNDER,
                "founderReserveGold", "76030000.00",
                "actualBurnedGold", "123.45",
                "modelOnly", true
        );
        Map<String, Object> snapshot = map(
                "snapshotHeight", 500,
                "snapshotBlockHash", "abc123",
                "founderBalanceGold", "76030000.00",
                "burnTransactionCreated", false,
                "modelOnly", true,
                "burnOptions", burnOptionRows(),
                "proofChecklist", map(
                        "founderAddress", Seting.ADDRESS_FOUNDER,
                        "burnAddress", "BURN_ADDRESS",
                        "burnTxHash", "PENDING_MANUAL_BURN"
                ),
                "note", "Burn options are modeled from the snapshot founder balance."
        );
        return V35DashboardApiClient.LiveData.of("http://node.test", protocol, supply, burn, founder, snapshot);
    }

    private static List<Map<String, Object>> burnOptionRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(map("percent", 75, "recommended", false, "burnGold", "57022500.00",
                "founderReserveAfterBurnGold", "19007500.00",
                "modeledBurnedGoldAfterBurn", "57022623.45",
                "modeledCirculatingGoldAfterBurn", "102527376.55",
                "modeledInflationAfterBurnPercent", "1.2304"));
        rows.add(map("percent", 80, "recommended", true, "burnGold", "60824000.00",
                "founderReserveAfterBurnGold", "15206000.00",
                "modeledBurnedGoldAfterBurn", "60824123.45",
                "modeledCirculatingGoldAfterBurn", "98725876.55",
                "modeledInflationAfterBurnPercent", "1.2777"));
        return rows;
    }

    private static Map<String, Object> map(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }
}
