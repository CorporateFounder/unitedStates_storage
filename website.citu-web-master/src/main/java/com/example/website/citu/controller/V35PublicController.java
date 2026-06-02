package com.example.website.citu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class V35PublicController {
    static final String V35_FORK_INDEX = "514291";
    static final String BURN_ADDRESS = "BURN_ADDRESS";
    static final String FOUNDER_ADDRESS = "nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43";
    static final String ANNUAL_EMISSION = "1,261,440 GOLD/year";
    static final String PLANNING_CANDIDATE_FORK_INDEX = "514291";

    private final V35FounderBurnProofApiClient founderBurnProofApiClient;

    public V35PublicController() {
        this(new V35FounderBurnProofApiClient());
    }

    V35PublicController(V35FounderBurnProofApiClient founderBurnProofApiClient) {
        this.founderBurnProofApiClient = founderBurnProofApiClient;
    }

    @GetMapping("/v35")
    public String overview(Model model) {
        addCommonV35Model(model, "V35 overview");
        return "v35";
    }

    @GetMapping("/v35/tokenomics")
    public String tokenomics(Model model) {
        addCommonV35Model(model, "V35 tokenomics");
        return "v35-tokenomics";
    }

    @GetMapping("/v35/burn")
    public String burn(Model model) {
        addCommonV35Model(model, "V35 burn policy");
        return "v35-burn";
    }

    @GetMapping("/v35/founder-burn-proof")
    public String founderBurnProof(Model model) {
        addCommonV35Model(model, "V35 founder burn proof");
        model.addAttribute("burnExecutionStatus", "Pending - not executed yet");
        model.addAttribute("proofPlaceholder", "Pending - to be published after snapshot and burn execution");
        model.addAttribute("recommendedBurnPercent", "80");
        addFounderBurnLiveStatus(model, founderBurnProofApiClient.fetchStatus());
        return "v35-founder-burn-proof";
    }

    @GetMapping("/v35/jackpot")
    public String jackpot(Model model) {
        addCommonV35Model(model, "V35 jackpot");
        return "v35-jackpot";
    }

    @GetMapping("/v35/mining")
    public String mining(Model model) {
        addCommonV35Model(model, "V35 mining");
        return "v35-mining";
    }

    @GetMapping("/v35/faq")
    public String faq(Model model) {
        addCommonV35Model(model, "V35 FAQ");
        return "v35-faq";
    }

    @GetMapping("/v35/preactivation-status")
    public String preactivationStatus(Model model) {
        addCommonV35Model(model, "V35 pre-activation status");
        model.addAttribute("candidateForkIndex", PLANNING_CANDIDATE_FORK_INDEX);
        return "v35-preactivation-status";
    }

    @GetMapping("/v35/operator-guide")
    public String operatorGuide(Model model) {
        addCommonV35Model(model, "V35 operator guide");
        return "v35-operator-guide";
    }

    @GetMapping("/v35/checksums")
    public String checksumVerification(Model model) {
        addCommonV35Model(model, "V35 checksum verification");
        return "v35-checksums";
    }

    @GetMapping("/v35/burn-warning")
    public String burnWarning(Model model) {
        addCommonV35Model(model, "V35 burn warning");
        return "v35-burn-warning";
    }

    private void addCommonV35Model(Model model, String title) {
        model.addAttribute("title", title);
        model.addAttribute("v35Active", false);
        model.addAttribute("v35ForkIndex", V35_FORK_INDEX);
        model.addAttribute("burnAddress", BURN_ADDRESS);
        model.addAttribute("founderAddress", FOUNDER_ADDRESS);
        model.addAttribute("blockTimeSeconds", "200");
        model.addAttribute("blocksPerDay", "432");
        model.addAttribute("rewardPerBlockGold", "8");
        model.addAttribute("minerRewardGold", "6");
        model.addAttribute("jackpotReserveGold", "2");
        model.addAttribute("founderRewardGold", "0");
        model.addAttribute("annualEmissionGold", ANNUAL_EMISSION);
        model.addAttribute("candidateForkIndex", PLANNING_CANDIDATE_FORK_INDEX);
    }

    private void addFounderBurnLiveStatus(Model model, V35FounderBurnProofApiClient.LiveStatus status) {
        model.addAttribute("liveApiBaseUrl", blankIfMissing(status.baseUrl()));
        model.addAttribute("liveApiStatus", status.overallStatus());
        model.addAttribute("liveApiWarning", status.warning());
        model.addAttribute("supplyApiStatus", status.endpointStatuses().get(V35FounderBurnProofApiClient.SUPPLY_PATH));
        model.addAttribute("burnApiStatus", status.endpointStatuses().get(V35FounderBurnProofApiClient.BURN_PATH));
        model.addAttribute("founderApiStatus", status.endpointStatuses().get(V35FounderBurnProofApiClient.FOUNDER_PATH));
        model.addAttribute("snapshotApiStatus", status.endpointStatuses().get(V35FounderBurnProofApiClient.BURN_SNAPSHOT_PATH));

        model.addAttribute("liveV35Active", liveV35Active(status));
        model.addAttribute("liveV35ForkIndex", firstAvailable(status.safeSupply(), status.safeBurn(), "v35ForkIndex", V35_FORK_INDEX));
        model.addAttribute("currentBurnStatus", currentBurnStatus(status));

        model.addAttribute("liveTotalMintedGold", V35FounderBurnProofApiClient.text(status.safeSupply(), "totalMintedGold", "Unavailable"));
        model.addAttribute("liveBurnedGold", firstAvailable(status.safeSupply(), status.safeBurn(), "burnedGold", "Unavailable"));
        model.addAttribute("liveCirculatingGold", V35FounderBurnProofApiClient.text(status.safeSupply(), "circulatingGold", "Unavailable"));
        model.addAttribute("liveFounderReserveGold", firstAvailable(status.safeSupply(), status.safeFounder(), "founderReserveGold", "Unavailable"));
        model.addAttribute("liveJackpotPaidGold", V35FounderBurnProofApiClient.text(status.safeSupply(), "jackpotPaidGold", "Unavailable"));
        model.addAttribute("liveAnnualEmissionGold", firstAvailable(status.safeSupply(), status.safeSnapshot(), "annualEmissionGold", ANNUAL_EMISSION));

        model.addAttribute("liveBurnAddress", firstAvailable(status.safeBurn(), status.safeSnapshot(), "burnAddress", BURN_ADDRESS));
        model.addAttribute("liveActualBurnedGold", firstAvailable(status.safeBurn(), status.safeFounder(), "actualBurnedGold", "Unavailable"));
        model.addAttribute("liveBurnTransactionCreated", String.valueOf(burnTransactionCreated(status)));
        model.addAttribute("liveFounderAddress", firstAvailable(status.safeFounder(), status.safeSnapshot(), "founderAddress", FOUNDER_ADDRESS));
        model.addAttribute("liveModeledBurnGold", V35FounderBurnProofApiClient.text(status.safeFounder(), "modeledBurnGold", "Unavailable"));

        model.addAttribute("liveSnapshotHeight", V35FounderBurnProofApiClient.text(status.safeSnapshot(), "snapshotHeight", "Unavailable"));
        model.addAttribute("liveSnapshotBlockHash", V35FounderBurnProofApiClient.text(status.safeSnapshot(), "snapshotBlockHash", "Unavailable"));
        model.addAttribute("liveFounderBalanceGold", V35FounderBurnProofApiClient.text(status.safeSnapshot(), "founderBalanceGold", "Unavailable"));
        model.addAttribute("liveRecommendedBurnPercent", V35FounderBurnProofApiClient.text(status.safeSnapshot(), "recommendedBurnPercent", "80"));
    }

    private String firstAvailable(java.util.Map<String, Object> first, java.util.Map<String, Object> second, String key, String fallback) {
        String value = V35FounderBurnProofApiClient.text(first, key, "");
        if (!value.isBlank()) {
            return value;
        }
        return V35FounderBurnProofApiClient.text(second, key, fallback);
    }

    private String liveV35Active(V35FounderBurnProofApiClient.LiveStatus status) {
        boolean active = V35FounderBurnProofApiClient.flag(status.safeSupply(), "v35Active",
                V35FounderBurnProofApiClient.flag(status.safeBurn(), "v35Active", false));
        return String.valueOf(active);
    }

    private String currentBurnStatus(V35FounderBurnProofApiClient.LiveStatus status) {
        if (burnTransactionCreated(status)) {
            return "API reports a burn transaction exists - verify published transaction proof before treating as final";
        }
        return "Planned / Pending / Not executed yet";
    }

    private boolean burnTransactionCreated(V35FounderBurnProofApiClient.LiveStatus status) {
        return V35FounderBurnProofApiClient.flag(status.safeBurn(), "burnTransactionCreated", false)
                || V35FounderBurnProofApiClient.flag(status.safeSnapshot(), "burnTransactionCreated", false);
    }

    private String blankIfMissing(String value) {
        return value == null || value.isBlank() ? "Unavailable" : value;
    }
}
