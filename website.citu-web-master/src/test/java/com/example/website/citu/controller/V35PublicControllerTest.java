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
        assertEquals("514291", model.get("candidateForkIndex"));

        assertEquals("v35-tokenomics", controller.tokenomics(new ExtendedModelMap()));
        assertEquals("v35-burn", controller.burn(new ExtendedModelMap()));
        assertEquals("v35-founder-burn-proof", controller.founderBurnProof(new ExtendedModelMap()));
        assertEquals("v35-jackpot", controller.jackpot(new ExtendedModelMap()));
        assertEquals("v35-mining", controller.mining(new ExtendedModelMap()));
        assertEquals("v35-faq", controller.faq(new ExtendedModelMap()));
        assertEquals("v35-preactivation-status", controller.preactivationStatus(new ExtendedModelMap()));
        assertEquals("v35-operator-guide", controller.operatorGuide(new ExtendedModelMap()));
        assertEquals("v35-checksums", controller.checksumVerification(new ExtendedModelMap()));
        assertEquals("v35-burn-warning", controller.burnWarning(new ExtendedModelMap()));
    }

    @Test
    void templatesContainRequiredPublicSafetyWording() throws IOException {
        List<String> files = List.of(
                "v35.html",
                "v35-tokenomics.html",
                "v35-burn.html",
                "v35-founder-burn-proof.html",
                "v35-jackpot.html",
                "v35-mining.html",
                "v35-faq.html",
                "v35-preactivation-status.html",
                "v35-operator-guide.html",
                "v35-checksums.html",
                "v35-burn-warning.html"
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
    void founderBurnProofPageUsesOnlyPendingProofValues() throws IOException {
        String content = Files.readString(template("v35-founder-burn-proof.html"));

        assertTrue(content.contains("Founder burn is planned, not executed yet"));
        assertTrue(content.contains("Optional live API status"));
        assertTrue(content.contains("Live values when available"));
        assertTrue(content.contains("The founder burn has not happened yet"));
        assertTrue(content.contains("nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43"));
        assertTrue(content.contains("BURN_ADDRESS"));
        assertTrue(content.contains("80% of snapshot founder balance"));
        assertTrue(content.contains("Founder reward after V35"));
        assertTrue(content.contains("Fork activation is by block index, not date alone"));
        assertTrue(content.contains("Pending - not executed yet"));
        assertTrue(content.contains("Pending - to be published after snapshot"));
        assertTrue(content.contains("burn tx hash"));
        assertTrue(content.contains("/v35/supply"));
        assertTrue(content.contains("/v35/burn"));
        assertTrue(content.contains("/v35/founder"));
        assertTrue(content.contains("/v35/founder/burn-snapshot"));
        assertTrue(content.contains("Modeled/recommended burn is not actual burned supply"));
        assertTrue(content.contains("not financial advice"));
        assertTrue(content.contains("Do not treat Pending fields as a real burn transaction"));
    }

    @Test
    void founderBurnProofFallsBackWhenApiUnavailable() {
        V35FounderBurnProofApiClient client = new V35FounderBurnProofApiClient(
                (url, timeoutMillis) -> {
                    throw new IOException("offline");
                },
                () -> "http://node.example"
        );
        V35PublicController fallbackController = new V35PublicController(client);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("v35-founder-burn-proof", fallbackController.founderBurnProof(model));
        assertEquals("Unavailable", model.get("liveApiStatus"));
        assertEquals("Unavailable", model.get("supplyApiStatus"));
        assertEquals("Unavailable", model.get("burnApiStatus"));
        assertEquals("Unavailable", model.get("founderApiStatus"));
        assertEquals("Unavailable", model.get("snapshotApiStatus"));
        assertEquals("Planned / Pending / Not executed yet", model.get("currentBurnStatus"));
        assertEquals("Unavailable", model.get("liveTotalMintedGold"));
        assertEquals("Unavailable", model.get("liveBurnedGold"));
        assertEquals("false", model.get("liveV35Active"));
        assertEquals("Long.MAX_VALUE", model.get("liveV35ForkIndex"));
    }

    @Test
    void founderBurnProofMapsOptionalLiveApiDataWithoutTreatingModeledBurnAsActual() {
        V35FounderBurnProofApiClient client = new V35FounderBurnProofApiClient(
                (url, timeoutMillis) -> {
                    if (url.endsWith(V35FounderBurnProofApiClient.SUPPLY_PATH)) {
                        return "{\"totalMintedGold\":\"100.00\",\"burnedGold\":\"0.00\",\"circulatingGold\":\"100.00\",\"founderReserveGold\":\"80.00\",\"jackpotPaidGold\":\"0.00\",\"annualEmissionGold\":\"1261440.00\",\"v35Active\":false,\"v35ForkIndex\":\"Long.MAX_VALUE\"}";
                    }
                    if (url.endsWith(V35FounderBurnProofApiClient.BURN_PATH)) {
                        return "{\"burnAddress\":\"BURN_ADDRESS\",\"burnTransactionCreated\":false,\"actualBurnedGold\":\"0.00\",\"burnedGold\":\"0.00\",\"v35Active\":false,\"v35ForkIndex\":\"Long.MAX_VALUE\"}";
                    }
                    if (url.endsWith(V35FounderBurnProofApiClient.FOUNDER_PATH)) {
                        return "{\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"founderReserveGold\":\"80.00\",\"modeledBurnGold\":\"64.00\",\"actualBurnedGold\":\"0.00\"}";
                    }
                    if (url.endsWith(V35FounderBurnProofApiClient.BURN_SNAPSHOT_PATH)) {
                        return "{\"snapshotHeight\":\"123\",\"snapshotBlockHash\":\"abc\",\"founderBalanceGold\":\"80.00\",\"recommendedBurnPercent\":\"80\",\"burnTransactionCreated\":false}";
                    }
                    throw new IOException("unexpected url");
                },
                () -> "http://node.example/"
        );
        V35PublicController liveController = new V35PublicController(client);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("v35-founder-burn-proof", liveController.founderBurnProof(model));
        assertEquals("Partial / available", model.get("liveApiStatus"));
        assertEquals("Available", model.get("supplyApiStatus"));
        assertEquals("Available", model.get("burnApiStatus"));
        assertEquals("Available", model.get("founderApiStatus"));
        assertEquals("Available", model.get("snapshotApiStatus"));
        assertEquals("false", model.get("liveV35Active"));
        assertEquals("0.00", model.get("liveBurnedGold"));
        assertEquals("100.00", model.get("liveCirculatingGold"));
        assertEquals("64.00", model.get("liveModeledBurnGold"));
        assertEquals("false", model.get("liveBurnTransactionCreated"));
        assertEquals("123", model.get("liveSnapshotHeight"));
        assertEquals("abc", model.get("liveSnapshotBlockHash"));
        assertEquals("Planned / Pending / Not executed yet", model.get("currentBurnStatus"));
    }

    @Test
    void founderBurnProofHandlesMalformedApiDataSafely() {
        V35FounderBurnProofApiClient client = new V35FounderBurnProofApiClient(
                (url, timeoutMillis) -> "{not-json",
                () -> "http://node.example"
        );
        V35PublicController malformedController = new V35PublicController(client);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("v35-founder-burn-proof", malformedController.founderBurnProof(model));
        assertEquals("Unavailable", model.get("liveApiStatus"));
        assertEquals("Unavailable", model.get("liveTotalMintedGold"));
        assertEquals("Planned / Pending / Not executed yet", model.get("currentBurnStatus"));
    }

    @Test
    void headerLinksToPublicV35Pages() throws IOException {
        String header = Files.readString(template("blocks/header.html"));

        assertTrue(header.contains("@{/images/citu-v35-logo.jpg}"));
        assertTrue(header.contains("alt=\"Official CITU logo\""));
        assertTrue(header.contains("href=\"/v35\""));
        assertTrue(header.contains("href=\"/v35/tokenomics\""));
        assertTrue(header.contains("href=\"/v35/burn-warning\""));
        assertTrue(header.contains("href=\"/v35/checksums\""));
        assertTrue(header.contains("href=\"/v35/founder-burn-proof\""));
        assertTrue(header.contains("href=\"/v35/jackpot\""));
        assertTrue(header.contains("href=\"/v35/preactivation-status\""));
        assertTrue(header.contains("href=\"/v35/operator-guide\""));
    }

    @Test
    void preactivationInstructionPagesKeepPublicWordingSafe() throws IOException {
        String content = Files.readString(template("v35-preactivation-status.html"))
                + Files.readString(template("v35-operator-guide.html"))
                + Files.readString(template("v35-checksums.html"))
                + Files.readString(template("v35-burn-warning.html"));

        assertTrue(content.contains("V35 is prepared but not active yet"));
        assertTrue(content.contains("planning-only"));
        assertTrue(content.contains("No user action is required yet"));
        assertTrue(content.contains("Final checksums are not published yet"));
        assertTrue(content.contains("SHA256SUMS.txt.sha256"));
        assertTrue(content.contains("Do not send coins to unofficial or fake burn addresses"));
        assertTrue(content.contains("No founder burn has occurred"));
        assertTrue(content.contains("514291"));
        assertFalse(content.contains("513332"));
        assertFalse(content.contains("513858"));
        assertTrue(content.contains("Official burn address has not been published yet"));
        assertTrue(content.contains("Do not send coins to any unofficial burn address"));
        assertTrue(content.contains("lang=\"en\""));
        assertTrue(content.contains("lang=\"ru\""));
        assertTrue(content.contains("lang=\"zh\""));
        assertTrue(content.contains("V35 подготовлен, но еще не активирован"));
        assertTrue(content.contains("Сжигание средств основателя не выполнялось"));
        assertTrue(content.contains("V35 已准备，但尚未激活"));
        assertTrue(content.contains("创始人销毁尚未执行"));
    }

    private Path template(String file) {
        return Path.of("src", "main", "resources", "templates", file);
    }
}
