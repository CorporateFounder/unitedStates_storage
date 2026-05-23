package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only response builder for V35 supply/protocol endpoints.
 */
public final class V35SupplyApiResponseBuilder {
    private static final int GOLD_SCALE = 2;
    private static final int PERCENT_SCALE = 4;
    private static final BigDecimal FOUNDER_BURN_PERCENT = new BigDecimal("80");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int[] BURN_OPTION_PERCENTS = new int[]{75, 80, 85, 90};

    private V35SupplyApiResponseBuilder() {
    }

    public static Map<String, Object> supply(V35SupplySnapshot snapshot, long currentHeight) {
        Map<String, Object> response = baseProtocolFields(currentHeight);
        response.put("totalMintedGold", gold(snapshot.getTotalMintedGold()));
        response.put("burnedGold", gold(snapshot.getBurnedGold()));
        response.put("circulatingGold", gold(snapshot.getCirculatingGold()));
        response.put("founderReserveGold", gold(snapshot.getFounderReserveGold()));
        response.put("jackpotPaidGold", gold(snapshot.getJackpotPaidGold()));
        response.put("annualEmissionGold", gold(snapshot.getAnnualEmissionGold()));
        response.put("valid", snapshot.isValid());
        response.put("invalidReason", safe(snapshot.getInvalidReason()));
        response.put("snapshotSource", "canonical_chain_and_account_map");
        return response;
    }

    public static Map<String, Object> protocol(long currentHeight) {
        Map<String, Object> response = baseProtocolFields(currentHeight);
        response.put("annualEmissionGold", gold(V35SupplyCalculator.annualEmissionGold()));
        return response;
    }

    public static Map<String, Object> burn(V35SupplySnapshot snapshot, long currentHeight) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("burnAddress", BurnAddressPolicy.getBurnAddress());
        response.put("isBurnAddressPlaceholder", BurnAddressPolicy.isPlaceholderBurnAddress());
        response.put("burnAddressConfigured", snapshot.isBurnAddressConfigured());
        response.put("burnedGold", gold(snapshot.getBurnedGold()));
        response.put("noSpendAfterV35", true);
        response.put("burnTransactionCreated", false);
        response.put("v35Active", v35Active(currentHeight));
        response.put("v35ForkIndex", forkIndexValue());
        response.put("note", "Actual burned supply is based on burn address balance, not modeled burn.");
        return response;
    }

    public static Map<String, Object> founder(V35SupplySnapshot snapshot, long currentHeight) {
        BigDecimal founderReserve = safeGold(snapshot.getFounderReserveGold());
        BigDecimal modeledBurn = founderReserve.multiply(FOUNDER_BURN_PERCENT)
                .divide(ONE_HUNDRED, GOLD_SCALE, RoundingMode.HALF_UP);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("founderAddress", Seting.ADDRESS_FOUNDER);
        response.put("founderReserveGold", gold(founderReserve));
        response.put("founderRewardGold", gold(Seting.V35_EXPECTED_FOUNDER_REWARD));
        response.put("modeledBurnPercent", "80");
        response.put("modeledBurnGold", gold(modeledBurn));
        response.put("actualBurnedGold", gold(snapshot.getBurnedGold()));
        response.put("modelOnly", true);
        response.put("v35Active", v35Active(currentHeight));
        response.put("v35ForkIndex", forkIndexValue());
        response.put("note", "Modeled burn is separate from actual burned supply and is not counted until a burn transaction exists.");
        return response;
    }

    public static Map<String, Object> founderBurnSnapshot(V35SupplySnapshot snapshot,
                                                          long snapshotHeight,
                                                          String snapshotBlockHash) {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> burnOptions = burnOptions(snapshot);
        Map<String, Object> recommendedOption = recommendedOption(burnOptions);

        response.put("founderAddress", Seting.ADDRESS_FOUNDER);
        response.put("burnAddress", BurnAddressPolicy.getBurnAddress());
        response.put("burnAddressConfigured", snapshot.isBurnAddressConfigured());
        response.put("isBurnAddressPlaceholder", BurnAddressPolicy.isPlaceholderBurnAddress());
        response.put("snapshotHeight", snapshotHeight);
        response.put("snapshotBlockHash", safeUnknown(snapshotBlockHash));
        response.put("founderBalanceGold", gold(snapshot.getFounderReserveGold()));
        response.put("totalMintedGold", gold(snapshot.getTotalMintedGold()));
        response.put("actualBurnedGold", gold(snapshot.getBurnedGold()));
        response.put("actualCirculatingGold", gold(snapshot.getCirculatingGold()));
        response.put("annualEmissionGold", gold(snapshot.getAnnualEmissionGold()));
        response.put("v35Active", v35Active(snapshotHeight));
        response.put("v35ForkIndex", forkIndexValue());
        response.put("recommendedBurnPercent", 80);
        response.put("burnTransactionCreated", false);
        response.put("modelOnly", true);
        response.put("burnOptions", burnOptions);
        response.put("proofChecklist", proofChecklist(response, recommendedOption));
        response.put("note", "Burn options are modeled from the snapshot founder balance; no burn transaction has been created.");
        return response;
    }

    private static Map<String, Object> baseProtocolFields(long currentHeight) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("v35Active", v35Active(currentHeight));
        response.put("v35ForkIndex", forkIndexValue());
        response.put("blockTimeSeconds", Seting.V35_TARGET_BLOCK_TIME_SECONDS);
        response.put("blocksPerDay", JackpotCalculator.dailyWindowBlocks());
        response.put("rewardPerBlockGold", gold(Seting.V35_GOLD_REWARD));
        response.put("minerRewardGold", gold(Seting.V35_INSTANT_MINER_REWARD));
        response.put("jackpotReservePerBlockGold", gold(Seting.V35_JACKPOT_REWARD));
        response.put("founderRewardGold", gold(Seting.V35_EXPECTED_FOUNDER_REWARD));
        response.put("burnAddress", BurnAddressPolicy.getBurnAddress());
        response.put("founderAddress", Seting.ADDRESS_FOUNDER);
        response.put("isBurnAddressPlaceholder", BurnAddressPolicy.isPlaceholderBurnAddress());
        response.put("burnAddressConfigured", !BurnAddressPolicy.isPlaceholderBurnAddress());
        return response;
    }

    private static List<Map<String, Object>> burnOptions(V35SupplySnapshot snapshot) {
        List<Map<String, Object>> options = new ArrayList<>();
        for (int percent : BURN_OPTION_PERCENTS) {
            options.add(burnOption(snapshot, percent));
        }
        return options;
    }

    private static Map<String, Object> burnOption(V35SupplySnapshot snapshot, int percent) {
        BigDecimal founderBalance = safeGold(snapshot.getFounderReserveGold());
        BigDecimal burnGold = founderBalance.multiply(BigDecimal.valueOf(percent))
                .divide(ONE_HUNDRED, GOLD_SCALE, RoundingMode.HALF_UP);
        BigDecimal founderReserve = safeGold(founderBalance.subtract(burnGold));
        BigDecimal modeledBurned = safeGold(snapshot.getBurnedGold()).add(burnGold).setScale(GOLD_SCALE, RoundingMode.UNNECESSARY);
        BigDecimal modeledCirculating = V35SupplyCalculator.circulatingGold(snapshot.getTotalMintedGold(), modeledBurned);
        BigDecimal modeledInflation = V35SupplyCalculator.inflationPercent(snapshot.getAnnualEmissionGold(), modeledCirculating);

        Map<String, Object> option = new LinkedHashMap<>();
        option.put("percent", percent);
        option.put("recommended", percent == FOUNDER_BURN_PERCENT.intValue());
        option.put("burnGold", gold(burnGold));
        option.put("founderReserveAfterBurnGold", gold(founderReserve));
        option.put("modeledBurnedGoldAfterBurn", gold(modeledBurned));
        option.put("modeledCirculatingGoldAfterBurn", gold(modeledCirculating));
        option.put("modeledInflationAfterBurnPercent", percent(modeledInflation));
        return option;
    }

    private static Map<String, Object> recommendedOption(List<Map<String, Object>> options) {
        for (Map<String, Object> option : options) {
            if (Boolean.TRUE.equals(option.get("recommended"))) {
                return option;
            }
        }
        return options.isEmpty() ? new LinkedHashMap<>() : options.get(0);
    }

    private static Map<String, Object> proofChecklist(Map<String, Object> response,
                                                      Map<String, Object> recommendedOption) {
        Map<String, Object> checklist = new LinkedHashMap<>();
        checklist.put("founderAddress", response.get("founderAddress"));
        checklist.put("burnAddress", response.get("burnAddress"));
        checklist.put("snapshotHeight", response.get("snapshotHeight"));
        checklist.put("snapshotBlockHash", response.get("snapshotBlockHash"));
        checklist.put("founderBalanceBeforeBurn", response.get("founderBalanceGold"));
        checklist.put("burnAmount", recommendedOption.get("burnGold"));
        checklist.put("burnTxHash", "PENDING_MANUAL_BURN");
        checklist.put("burnBlockIndex", "PENDING_MANUAL_BURN");
        checklist.put("burnBlockHash", "PENDING_MANUAL_BURN");
        checklist.put("founderBalanceAfterBurn", recommendedOption.get("founderReserveAfterBurnGold"));
        checklist.put("burnedSupply", recommendedOption.get("modeledBurnedGoldAfterBurn"));
        checklist.put("circulatingSupply", recommendedOption.get("modeledCirculatingGoldAfterBurn"));
        checklist.put("annualEmission", response.get("annualEmissionGold"));
        checklist.put("inflationAfterBurn", recommendedOption.get("modeledInflationAfterBurnPercent"));
        return checklist;
    }

    private static boolean v35Active(long currentHeight) {
        return Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE
                && currentHeight >= Seting.V35_RESCUE_FORK_INDEX;
    }

    private static Object forkIndexValue() {
        if (Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE) {
            return "Long.MAX_VALUE";
        }
        return Seting.V35_RESCUE_FORK_INDEX;
    }

    private static String gold(double value) {
        return V35SupplyCalculator.gold(value).toPlainString();
    }

    private static String gold(BigDecimal value) {
        return safeGold(value).toPlainString();
    }

    private static String percent(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(PERCENT_SCALE).toPlainString();
        }
        return value.setScale(PERCENT_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    private static BigDecimal safeGold(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(GOLD_SCALE);
        }
        return value.setScale(GOLD_SCALE, RoundingMode.HALF_UP);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeUnknown(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return value;
    }
}
