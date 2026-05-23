package International_Trade_Union.controllers;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35DashboardControllerTest {
    @Test
    public void dashboardRouteRendersWhileV35Disabled() {
        V35DashboardController controller = new V35DashboardController();
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.dashboard(model);

        assertEquals("v35-dashboard", view);
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals(false, model.get("v35Active"));
        assertEquals("Long.MAX_VALUE", model.get("v35ForkIndex"));
        assertEquals("BURN_ADDRESS", model.get("burnAddress"));
        assertEquals(true, model.get("burnAddressConfigured"));
        assertEquals(false, model.get("isBurnAddressPlaceholder"));
    }

    @Test
    public void dashboardShowsCanonicalV35EconomicsAndUnavailableLiveSupply() {
        V35DashboardController controller = new V35DashboardController();
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
    }

    @Test
    public void dashboardContainsBurnOptionsAndJackpotPolicy() {
        V35DashboardController controller = new V35DashboardController();
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
    public void dashboardTemplateContainsSafetyWordingAndHeaderLink() throws Exception {
        String template = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/v35-dashboard.html")),
                StandardCharsets.UTF_8);
        String header = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/blocks/header.html")),
                StandardCharsets.UTF_8);

        assertTrue(template.contains("V35 is not active while V35_RESCUE_FORK_INDEX is Long.MAX_VALUE."));
        assertTrue(template.contains("Modeled burn values are projections."));
        assertTrue(template.contains("Actual supply and burn"));
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
    private static List<Map<String, String>> jackpotPeriods(ExtendedModelMap model) {
        return (List<Map<String, String>>) model.get("jackpotPeriods");
    }
}
