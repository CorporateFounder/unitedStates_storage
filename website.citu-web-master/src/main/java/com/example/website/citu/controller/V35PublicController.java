package com.example.website.citu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class V35PublicController {
    static final String V35_FORK_INDEX = "Long.MAX_VALUE";
    static final String BURN_ADDRESS = "BURN_ADDRESS";
    static final String FOUNDER_ADDRESS = "nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43";
    static final String ANNUAL_EMISSION = "1,261,440 GOLD/year";

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
    }
}
