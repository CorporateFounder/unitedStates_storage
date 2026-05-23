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

        model.addAttribute("liveSupplyAvailable", false);
        model.addAttribute("unavailable", UNAVAILABLE);
        model.addAttribute("totalMintedGold", UNAVAILABLE);
        model.addAttribute("burnedGold", UNAVAILABLE);
        model.addAttribute("circulatingGold", UNAVAILABLE);
        model.addAttribute("founderReserveGold", UNAVAILABLE);
        model.addAttribute("jackpotPaidGold", UNAVAILABLE);
        model.addAttribute("snapshotHeight", UNAVAILABLE);
        model.addAttribute("snapshotBlockHash", UNAVAILABLE);
        model.addAttribute("founderBalanceGold", UNAVAILABLE);
        model.addAttribute("burnTransactionCreated", false);

        model.addAttribute("burnOptions", burnOptions());
        model.addAttribute("proofChecklist", proofChecklist());
        model.addAttribute("jackpotPeriods", jackpotPeriods());
        model.addAttribute("warnings", warnings());
        return "v35-dashboard";
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
}
