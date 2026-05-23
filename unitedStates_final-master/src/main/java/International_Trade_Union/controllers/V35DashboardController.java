package International_Trade_Union.controllers;

import International_Trade_Union.protocol.v35.BurnAddressPolicy;
import International_Trade_Union.protocol.v35.JackpotCalculator;
import International_Trade_Union.protocol.v35.V35SupplyCalculator;
import International_Trade_Union.setings.Seting;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class V35DashboardController {
    private static final String UNAVAILABLE = "Unavailable";
    private final V35DashboardApiClient apiClient;

    public V35DashboardController() {
        this(new V35DashboardApiClient());
    }

    V35DashboardController(V35DashboardApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/v35-dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "V35 dashboard");
        model.addAttribute("v35Active", isV35Active());
        model.addAttribute("v35ForkIndex", forkIndexValue());
        model.addAttribute("blockTimeSeconds", Seting.V35_TARGET_BLOCK_TIME_SECONDS);
        model.addAttribute("blocksPerDay", JackpotCalculator.dailyWindowBlocks());
        model.addAttribute("rewardPerBlockGold", gold(Seting.V35_GOLD_REWARD));
        model.addAttribute("minerRewardGold", gold(Seting.V35_INSTANT_MINER_REWARD));
        model.addAttribute("jackpotReservePerBlockGold", gold(Seting.V35_JACKPOT_REWARD));
        model.addAttribute("founderRewardGold", gold(Seting.V35_EXPECTED_FOUNDER_REWARD));
        model.addAttribute("annualEmissionGold", V35SupplyCalculator.annualEmissionGold().toPlainString());
        model.addAttribute("burnAddress", BurnAddressPolicy.getBurnAddress());
        model.addAttribute("burnAddressConfigured", !BurnAddressPolicy.isPlaceholderBurnAddress());
        model.addAttribute("isBurnAddressPlaceholder", BurnAddressPolicy.isPlaceholderBurnAddress());
        model.addAttribute("founderAddress", Seting.ADDRESS_FOUNDER);

        model.addAttribute("liveApiBaseUrl", UNAVAILABLE);
        model.addAttribute("liveApiError", "");
        model.addAttribute("liveProtocolAvailable", false);
        model.addAttribute("liveSupplyAvailable", false);
        model.addAttribute("liveBurnAvailable", false);
        model.addAttribute("liveFounderAvailable", false);
        model.addAttribute("liveFounderSnapshotAvailable", false);
        model.addAttribute("unavailable", UNAVAILABLE);
        model.addAttribute("totalMintedGold", UNAVAILABLE);
        model.addAttribute("burnedGold", UNAVAILABLE);
        model.addAttribute("circulatingGold", UNAVAILABLE);
        model.addAttribute("founderReserveGold", UNAVAILABLE);
        model.addAttribute("jackpotPaidGold", UNAVAILABLE);
        model.addAttribute("supplySnapshotSource", UNAVAILABLE);
        model.addAttribute("supplyValid", UNAVAILABLE);
        model.addAttribute("supplyInvalidReason", "");
        model.addAttribute("snapshotHeight", UNAVAILABLE);
        model.addAttribute("snapshotBlockHash", UNAVAILABLE);
        model.addAttribute("founderBalanceGold", UNAVAILABLE);
        model.addAttribute("founderSnapshotNote", "");
        model.addAttribute("burnTransactionCreated", false);

        model.addAttribute("burnOptions", burnOptions());
        model.addAttribute("proofChecklist", proofChecklist());
        model.addAttribute("jackpotPeriods", jackpotPeriods());
        model.addAttribute("warnings", warnings());
        overlayLiveData(model, apiClient.fetch());
        return "v35-dashboard";
    }

    private void overlayLiveData(Model model, V35DashboardApiClient.LiveData liveData) {
        if (liveData == null) {
            model.addAttribute("liveApiError", "Live V35 data unavailable.");
            return;
        }
        model.addAttribute("liveApiBaseUrl", fallback(liveData.baseUrl(), UNAVAILABLE));
        model.addAttribute("liveApiError", liveData.errorMessage());
        model.addAttribute("liveProtocolAvailable", liveData.protocolAvailable());
        model.addAttribute("liveSupplyAvailable", liveData.supplyAvailable());
        model.addAttribute("liveBurnAvailable", liveData.burnAvailable());
        model.addAttribute("liveFounderAvailable", liveData.founderAvailable());
        model.addAttribute("liveFounderSnapshotAvailable", liveData.founderBurnSnapshotAvailable());

        if (liveData.protocolAvailable()) {
            overlayProtocol(model, liveData.protocol());
        }
        if (liveData.supplyAvailable()) {
            overlaySupply(model, liveData.supply());
        }
        if (liveData.burnAvailable()) {
            overlayBurn(model, liveData.burn());
        }
        if (liveData.founderAvailable()) {
            overlayFounder(model, liveData.founder());
        }
        if (liveData.founderBurnSnapshotAvailable()) {
            overlayFounderBurnSnapshot(model, liveData.founderBurnSnapshot());
        }
    }

    private void overlayProtocol(Model model, Map<String, Object> protocol) {
        model.addAttribute("v35Active", V35DashboardApiClient.booleanField(protocol, "v35Active", isV35Active()));
        model.addAttribute("v35ForkIndex", V35DashboardApiClient.stringField(protocol, "v35ForkIndex", forkIndexValue()));
        model.addAttribute("blockTimeSeconds", V35DashboardApiClient.stringField(protocol, "blockTimeSeconds",
                Integer.toString(Seting.V35_TARGET_BLOCK_TIME_SECONDS)));
        model.addAttribute("blocksPerDay", V35DashboardApiClient.stringField(protocol, "blocksPerDay",
                Long.toString(JackpotCalculator.dailyWindowBlocks())));
        model.addAttribute("rewardPerBlockGold", V35DashboardApiClient.stringField(protocol, "rewardPerBlockGold",
                gold(Seting.V35_GOLD_REWARD)));
        model.addAttribute("minerRewardGold", V35DashboardApiClient.stringField(protocol, "minerRewardGold",
                gold(Seting.V35_INSTANT_MINER_REWARD)));
        model.addAttribute("jackpotReservePerBlockGold", V35DashboardApiClient.stringField(protocol, "jackpotReservePerBlockGold",
                gold(Seting.V35_JACKPOT_REWARD)));
        model.addAttribute("founderRewardGold", V35DashboardApiClient.stringField(protocol, "founderRewardGold",
                gold(Seting.V35_EXPECTED_FOUNDER_REWARD)));
        model.addAttribute("annualEmissionGold", V35DashboardApiClient.stringField(protocol, "annualEmissionGold",
                V35SupplyCalculator.annualEmissionGold().toPlainString()));
        model.addAttribute("burnAddress", V35DashboardApiClient.stringField(protocol, "burnAddress",
                BurnAddressPolicy.getBurnAddress()));
        model.addAttribute("founderAddress", V35DashboardApiClient.stringField(protocol, "founderAddress",
                Seting.ADDRESS_FOUNDER));
        model.addAttribute("burnAddressConfigured", V35DashboardApiClient.booleanField(protocol, "burnAddressConfigured",
                !BurnAddressPolicy.isPlaceholderBurnAddress()));
        model.addAttribute("isBurnAddressPlaceholder", V35DashboardApiClient.booleanField(protocol,
                "isBurnAddressPlaceholder", BurnAddressPolicy.isPlaceholderBurnAddress()));
    }

    private void overlaySupply(Model model, Map<String, Object> supply) {
        model.addAttribute("totalMintedGold", V35DashboardApiClient.stringField(supply, "totalMintedGold", UNAVAILABLE));
        model.addAttribute("burnedGold", V35DashboardApiClient.stringField(supply, "burnedGold", UNAVAILABLE));
        model.addAttribute("circulatingGold", V35DashboardApiClient.stringField(supply, "circulatingGold", UNAVAILABLE));
        model.addAttribute("founderReserveGold", V35DashboardApiClient.stringField(supply, "founderReserveGold", UNAVAILABLE));
        model.addAttribute("jackpotPaidGold", V35DashboardApiClient.stringField(supply, "jackpotPaidGold", UNAVAILABLE));
        model.addAttribute("annualEmissionGold", V35DashboardApiClient.stringField(supply, "annualEmissionGold",
                V35SupplyCalculator.annualEmissionGold().toPlainString()));
        model.addAttribute("supplySnapshotSource", V35DashboardApiClient.stringField(supply, "snapshotSource", UNAVAILABLE));
        model.addAttribute("supplyValid", V35DashboardApiClient.stringField(supply, "valid", UNAVAILABLE));
        model.addAttribute("supplyInvalidReason", V35DashboardApiClient.stringField(supply, "invalidReason", ""));
    }

    private void overlayBurn(Model model, Map<String, Object> burn) {
        model.addAttribute("burnAddress", V35DashboardApiClient.stringField(burn, "burnAddress",
                BurnAddressPolicy.getBurnAddress()));
        model.addAttribute("burnAddressConfigured", V35DashboardApiClient.booleanField(burn, "burnAddressConfigured",
                !BurnAddressPolicy.isPlaceholderBurnAddress()));
        model.addAttribute("isBurnAddressPlaceholder", V35DashboardApiClient.booleanField(burn,
                "isBurnAddressPlaceholder", BurnAddressPolicy.isPlaceholderBurnAddress()));
        model.addAttribute("burnTransactionCreated", V35DashboardApiClient.booleanField(burn,
                "burnTransactionCreated", false));
        if (UNAVAILABLE.equals(model.asMap().get("burnedGold"))) {
            model.addAttribute("burnedGold", V35DashboardApiClient.stringField(burn, "burnedGold", UNAVAILABLE));
        }
    }

    private void overlayFounder(Model model, Map<String, Object> founder) {
        model.addAttribute("founderAddress", V35DashboardApiClient.stringField(founder, "founderAddress",
                Seting.ADDRESS_FOUNDER));
        if (UNAVAILABLE.equals(model.asMap().get("founderReserveGold"))) {
            model.addAttribute("founderReserveGold", V35DashboardApiClient.stringField(founder,
                    "founderReserveGold", UNAVAILABLE));
        }
    }

    private void overlayFounderBurnSnapshot(Model model, Map<String, Object> snapshot) {
        model.addAttribute("snapshotHeight", V35DashboardApiClient.stringField(snapshot, "snapshotHeight", UNAVAILABLE));
        model.addAttribute("snapshotBlockHash", V35DashboardApiClient.stringField(snapshot, "snapshotBlockHash", UNAVAILABLE));
        model.addAttribute("founderBalanceGold", V35DashboardApiClient.stringField(snapshot, "founderBalanceGold", UNAVAILABLE));
        model.addAttribute("founderSnapshotNote", V35DashboardApiClient.stringField(snapshot, "note", ""));
        model.addAttribute("burnTransactionCreated", V35DashboardApiClient.booleanField(snapshot,
                "burnTransactionCreated", false));

        List<Map<String, Object>> liveBurnOptions = V35DashboardApiClient.listOfMapsField(snapshot, "burnOptions");
        if (!liveBurnOptions.isEmpty()) {
            model.addAttribute("burnOptions", liveBurnOptions);
        }
        List<String> liveProofChecklist = V35DashboardApiClient.proofChecklistField(snapshot, "proofChecklist");
        if (!liveProofChecklist.isEmpty()) {
            model.addAttribute("proofChecklist", liveProofChecklist);
        }
    }

    private boolean isV35Active() {
        return Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE;
    }

    private String forkIndexValue() {
        if (Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE) {
            return "Long.MAX_VALUE";
        }
        return Long.toString(Seting.V35_RESCUE_FORK_INDEX);
    }

    private List<Map<String, Object>> burnOptions() {
        List<Map<String, Object>> options = new ArrayList<>();
        int[] percents = new int[]{75, 80, 85, 90};
        for (int percent : percents) {
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("percent", percent);
            option.put("recommended", percent == 80);
            option.put("burnGold", UNAVAILABLE);
            option.put("founderReserveAfterBurnGold", UNAVAILABLE);
            option.put("modeledBurnedGoldAfterBurn", UNAVAILABLE);
            option.put("modeledCirculatingGoldAfterBurn", UNAVAILABLE);
            option.put("modeledInflationAfterBurnPercent", UNAVAILABLE);
            options.add(option);
        }
        return options;
    }

    private List<Map<String, String>> jackpotPeriods() {
        List<Map<String, String>> periods = new ArrayList<>();
        addPeriod(periods, "daily", JackpotCalculator.dailyWindowBlocks(), JackpotCalculator.JackpotPeriod.DAILY);
        addPeriod(periods, "weekly", JackpotCalculator.weeklyWindowBlocks(), JackpotCalculator.JackpotPeriod.WEEKLY);
        addPeriod(periods, "monthly", JackpotCalculator.monthlyWindowBlocks(), JackpotCalculator.JackpotPeriod.MONTHLY);
        addPeriod(periods, "quarterly", JackpotCalculator.quarterlyWindowBlocks(), JackpotCalculator.JackpotPeriod.QUARTERLY);
        addPeriod(periods, "half-year", JackpotCalculator.halfYearWindowBlocks(), JackpotCalculator.JackpotPeriod.HALF_YEAR);
        addPeriod(periods, "yearly", JackpotCalculator.yearlyWindowBlocks(), JackpotCalculator.JackpotPeriod.YEARLY);
        return periods;
    }

    private void addPeriod(List<Map<String, String>> periods,
                           String name,
                           long windowBlocks,
                           JackpotCalculator.JackpotPeriod period) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("windowBlocks", Long.toString(windowBlocks));
        row.put("reservePerBlockGold", gold(JackpotCalculator.reservePerBlockForPeriod(name)));
        row.put("payoutGold", JackpotCalculator.expectedPayoutAmount(period).toPlainString());
        periods.add(row);
    }

    private List<String> proofChecklist() {
        List<String> checklist = new ArrayList<>();
        checklist.add("founderAddress");
        checklist.add("burnAddress");
        checklist.add("snapshotHeight");
        checklist.add("snapshotBlockHash");
        checklist.add("founderBalanceBeforeBurn");
        checklist.add("burnAmount");
        checklist.add("burnTxHash");
        checklist.add("burnBlockIndex");
        checklist.add("burnBlockHash");
        checklist.add("founderBalanceAfterBurn");
        checklist.add("burnedSupply");
        checklist.add("circulatingSupply");
        checklist.add("annualEmission");
        checklist.add("inflationAfterBurn");
        return checklist;
    }

    private List<String> warnings() {
        List<String> warnings = new ArrayList<>();
        warnings.add("V35 is not active while V35_RESCUE_FORK_INDEX is Long.MAX_VALUE.");
        warnings.add("Founder burn has not happened until a burn transaction hash and burn block are published.");
        warnings.add("Modeled founder burn values are projections, not actual burned supply.");
        warnings.add("BURN_ADDRESS is a protocol sink. Sending to it is allowed; spending from it is blocked after V35.");
        warnings.add("Manual difficulty is legacy-only after V35.");
        return warnings;
    }

    private String gold(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String fallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
