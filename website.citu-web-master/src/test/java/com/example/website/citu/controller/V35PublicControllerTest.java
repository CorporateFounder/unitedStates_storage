package com.example.website.citu.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class V35PublicControllerTest {
    private final V35PublicController controller = new V35PublicController();

    @Test
    void routesRenderExpectedTemplatesAndStaticV35Model() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("v35", controller.overview(model));
        assertFalse((Boolean) model.get("v35Active"));
        assertEquals("Long.MAX_VALUE", model.get("v35ForkIndex"));
        assertEquals("BURN_ADDRESS", model.get("burnAddress"));
        assertEquals("200", model.get("blockTimeSeconds"));
        assertEquals("432", model.get("blocksPerDay"));
        assertEquals("8", model.get("rewardPerBlockGold"));
        assertEquals("6", model.get("minerRewardGold"));
        assertEquals("2", model.get("jackpotReserveGold"));
        assertEquals("0", model.get("founderRewardGold"));
        assertEquals("1,261,440 GOLD/year", model.get("annualEmissionGold"));

        assertEquals("v35-tokenomics", controller.tokenomics(new ExtendedModelMap()));
        assertEquals("v35-burn", controller.burn(new ExtendedModelMap()));
        assertEquals("v35-jackpot", controller.jackpot(new ExtendedModelMap()));
        assertEquals("v35-mining", controller.mining(new ExtendedModelMap()));
        assertEquals("v35-faq", controller.faq(new ExtendedModelMap()));
    }

    @Test
    void templatesContainRequiredPublicSafetyWording() throws IOException {
        List<String> files = List.of(
                "v35.html",
                "v35-tokenomics.html",
                "v35-burn.html",
                "v35-jackpot.html",
                "v35-mining.html",
                "v35-faq.html"
        );

        StringBuilder all = new StringBuilder();
        for (String file : files) {
            all.append(Files.readString(template(file))).append('\n');
        }

        String content = all.toString();
        assertTrue(content.contains("V35 is not active while V35_RESCUE_FORK_INDEX is Long.MAX_VALUE."));
        assertTrue(content.contains("BURN_ADDRESS"));
        assertTrue(content.contains("8 GOLD/block"));
        assertTrue(content.contains("6 GOLD"));
        assertTrue(content.contains("2 GOLD"));
        assertTrue(content.contains("0 founder"));
        assertTrue(content.contains("200 sec/block"));
        assertTrue(content.contains("432 blocks/day"));
        assertTrue(content.contains("1,261,440 GOLD/year"));
        assertTrue(content.contains("Actual vs modeled supply"));
        assertTrue(content.contains("The founder burn has not happened"));
        assertTrue(content.contains("302.40 GOLD"));
        assertTrue(content.contains("1209.60 GOLD"));
        assertTrue(content.contains("3888.00 GOLD"));
        assertTrue(content.contains("9720.00 GOLD"));
        assertTrue(content.contains("15724.80 GOLD"));
        assertTrue(content.contains("23652.00 GOLD"));
        assertTrue(content.contains("Sender addresses are never jackpot participants."));
        assertTrue(content.contains("No live winners are shown here"));
    }

    @Test
    void headerLinksToPublicV35Pages() throws IOException {
        String header = Files.readString(template("blocks/header.html"));

        assertTrue(header.contains("href=\"/v35\""));
        assertTrue(header.contains("href=\"/v35/tokenomics\""));
        assertTrue(header.contains("href=\"/v35/burn\""));
        assertTrue(header.contains("href=\"/v35/jackpot\""));
    }

    private Path template(String file) {
        return Path.of("src", "main", "resources", "templates", file);
    }
}
