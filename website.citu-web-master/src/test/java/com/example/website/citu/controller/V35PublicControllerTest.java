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
        assertEquals("514291", model.get("v35ForkIndex"));
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
        assertTrue(content.contains("V35 activation is scheduled"));
        assertFalse(content.contains("V35_RESCUE_FORK_INDEX is Long.MAX_VALUE"));
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
        assertEquals("514291", model.get("liveV35ForkIndex"));
    }

    @Test
    void founderBurnProofMapsOptionalLiveApiDataWithoutTreatingModeledBurnAsActual() {
        V35FounderBurnProofApiClient client = new V35FounderBurnProofApiClient(
                (url, timeoutMillis) -> {
                    if (url.endsWith(V35FounderBurnProofApiClient.SUPPLY_PATH)) {
                        return "{\"totalMintedGold\":\"100.00\",\"burnedGold\":\"0.00\",\"circulatingGold\":\"100.00\",\"founderReserveGold\":\"80.00\",\"jackpotPaidGold\":\"0.00\",\"annualEmissionGold\":\"1261440.00\",\"v35Active\":false,\"v35ForkIndex\":514291}";
                    }
                    if (url.endsWith(V35FounderBurnProofApiClient.BURN_PATH)) {
                        return "{\"burnAddress\":\"BURN_ADDRESS\",\"burnTransactionCreated\":false,\"actualBurnedGold\":\"0.00\",\"burnedGold\":\"0.00\",\"v35Active\":false,\"v35ForkIndex\":514291}";
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
        assertTrue(header.contains("href=\"/conductor\""));
        assertTrue(header.contains("href=\"/wallet_and_node_url\""));
        assertTrue(header.contains("href=\"/white_papper\""));
        assertTrue(header.contains("href=\"/charter\""));
        assertTrue(header.contains("href=\"/how_to_install\""));
        assertTrue(header.contains("href=\"/v35\""));
        assertTrue(header.contains("href=\"/v35/tokenomics\""));
        assertTrue(header.contains("href=\"/v35/burn-warning\""));
        assertTrue(header.contains("href=\"/v35/checksums\""));
        assertTrue(header.contains("href=\"/v35/founder-burn-proof\""));
        assertTrue(header.contains("href=\"/v35/jackpot\""));
        assertTrue(header.contains("href=\"/v35/preactivation-status\""));
        assertTrue(header.contains("href=\"/v35/operator-guide\""));
        assertTrue(header.contains("href=\"/v35/faq\""));
    }

    @Test
    void apiDocumentationKeepsExchangeIntegrationSafe() throws IOException {
        String content = Files.readString(template("wallet_and_node_url.html"));

        assertTrue(content.contains("Exchange / Operator API Documentation"));
        assertTrue(content.contains("GET /size"));
        assertTrue(content.contains("GET /prevBlock"));
        assertTrue(content.contains("POST /statusTransaction58"));
        assertTrue(content.contains("GET /v35/protocol"));
        assertTrue(content.contains("GET /v35/supply"));
        assertTrue(content.contains("GET /v35/jackpot/status"));
        assertTrue(content.contains("legacy local-only"));
        assertTrue(content.contains("Never place private keys or passwords in production URLs"));
        assertTrue(content.contains("Node/server 1.0.15 Stable"));
        assertTrue(content.contains("wallet/miner 1.0.2"));
        assertTrue(content.contains("Website/operator guide 1.0.7 contains the current Node 1.0.15 Stable link update"));
        assertTrue(content.contains("Recommended V35 node 1.0.15 Stable / wallet 1.0.2 downloads"));
        assertTrue(content.contains("https://github.com/CorporateFounder/unitedStates_storage"));
        assertTrue(content.contains("https://github.com/CorporateFounder/unitedStates_final"));
        assertTrue(content.contains("https://github.com/CorporateFounder/unitedStates_storage/releases"));
        assertTrue(content.contains("https://github.com/CorporateFounder/unitedStates_final/releases"));
        assertTrue(content.contains("citu-node-v35-activation-514291-1.0.15.jar"));
        assertTrue(content.contains("citu-wallet-v35-activation-514291-1.0.2.jar"));
        assertTrue(content.contains("fe1955a34c03f819149713a792d36155483db86bccd57f1506a1e3a7e57e5444"));
        assertTrue(content.contains("b2ea4d41925326a8b44115177db17a06ee25e6bfab0e832d7da2153313196179"));
        assertTrue(content.contains("-Dserver.port=82"));
        assertTrue(content.contains("-Dserver.port=8082"));
        assertTrue(content.contains("releases/tag/v35-node-activation-514291-1.0.15"));
        assertTrue(content.contains("releases/tag/v35-wallet-activation-514291-1.0.2"));
        assertTrue(content.contains("releases/download/v35-node-activation-514291-1.0.15"));
        assertTrue(content.contains("releases/download/v35-wallet-activation-514291-1.0.2"));
        assertTrue(content.contains("stable memory-hardened pre-activation update"));
        assertFalse(content.contains("will work after final GitHub Release is published"));
        assertTrue(content.contains("Important API request and response examples"));
        assertTrue(content.contains("SAFE READ-ONLY"));
        assertTrue(content.contains("V35 READ-ONLY"));
        assertTrue(content.contains("REVIEW SIGNED JSON"));
        assertTrue(content.contains("\"digitalDollar\": 0.01"));
        assertTrue(content.contains("\"burnAddressConfigured\":false"));
        assertTrue(content.contains("{\"v35Active\":false,\"v35ForkIndex\":514291}"));
        assertTrue(content.contains("GET /isTransactionAdd?sign=&lt;BASE58_SIGNATURE&gt;"));
        assertTrue(content.contains("internal placeholder"));
        assertTrue(content.contains("BURN_ADDRESS"));
        assertTrue(content.contains("internal placeholder"));

        String overview = Files.readString(template("v35.html"));
        assertTrue(overview.contains("/wallet_and_node_url#official-v35-release-downloads"));

        String releaseLinks = Files.readString(template("blocks/v35-release-links.html"));
        assertTrue(releaseLinks.contains("Download Node JAR"));
        assertTrue(releaseLinks.contains("Download Wallet JAR"));
        assertTrue(releaseLinks.contains("Node GitHub Release"));
        assertTrue(releaseLinks.contains("Wallet GitHub Release"));
        assertTrue(releaseLinks.contains("Node SHA256"));
        assertTrue(releaseLinks.contains("Wallet SHA256"));
        assertTrue(releaseLinks.contains("Website/operator guide <code>1.0.7</code> contains the current Node 1.0.15 Stable link update"));
        assertTrue(releaseLinks.contains("emergency stability fix after Node 1.0.12 OOM"));
        assertTrue(releaseLinks.contains("bounds peer diagnostic state"));
        assertTrue(releaseLinks.contains("bounds temporary peer work"));
        assertTrue(releaseLinks.contains("memory guard for non-critical peer work"));
        assertTrue(releaseLinks.contains("peer/network limits"));
        assertTrue(releaseLinks.contains("hard node log cap targeting under 10MB"));
        assertTrue(releaseLinks.contains("Diagnostics and heavy forensics are disabled by default"));
        assertTrue(releaseLinks.contains("Founder burn is deferred until after the network fully transitions to V35 and remains stable."));
        assertTrue(releaseLinks.contains("https://github.com/CorporateFounder/unitedStates_storage/releases/download/v35-node-activation-514291-1.0.15/citu-node-v35-activation-514291-1.0.15.jar"));
        assertTrue(releaseLinks.contains("https://github.com/CorporateFounder/unitedStates_final/releases/download/v35-wallet-activation-514291-1.0.2/citu-wallet-v35-activation-514291-1.0.2.jar"));
    }

    @Test
    void publicVisualLayerPresentsUsefulHomepageMetricsAndDarkTransactionCards() throws IOException {
        String homepage = Files.readString(template("main.html"));
        String footer = Files.readString(template("blocks/footer.html"));
        String transactions = Files.readString(template("detail-transactions.html"));
        String jackpot = Files.readString(template("v35-jackpot.html"));
        String styles = Files.readString(Path.of("src", "main", "resources", "static", "css", "styles.css"));

        assertTrue(homepage.contains("V35 PRE-ACTIVATION"));
        assertTrue(homepage.contains("Active / circulating GOLD supply"));
        assertTrue(homepage.contains("Burned GOLD"));
        assertTrue(homepage.contains("Legacy total GOLD endpoint value"));
        assertTrue(homepage.contains("8 GOLD/block"));
        assertTrue(homepage.contains("6 GOLD"));
        assertTrue(homepage.contains("2 GOLD"));
        assertTrue(homepage.contains("0 GOLD"));
        assertTrue(homepage.contains("No winner yet before V35 activation"));
        assertTrue(homepage.contains("Legacy / pre-V35 technical details"));
        assertFalse(homepage.contains("Every 4 month the multiplier decreases"));
        assertFalse(homepage.contains("target uniq sender in prev block"));
        assertTrue(jackpot.contains("Disabled until V35 activation"));
        assertTrue(jackpot.contains("Expected jackpot schedule after V35"));
        assertTrue(footer.contains("CITU protocol"));
        assertTrue(footer.contains("Official source"));
        assertTrue(footer.contains("Never send funds to <code>BURN_ADDRESS</code>"));
        assertTrue(transactions.contains("citu-transaction-card"));
        assertTrue(transactions.contains("citu-transaction-row"));
        assertFalse(transactions.contains("alert alert-info mt-2"));
        assertTrue(styles.contains(".v35-site-footer footer"));
        assertTrue(styles.contains(".citu-api-card"));
    }

    @Test
    void preactivationInstructionPagesKeepPublicWordingSafe() throws IOException {
        String content = Files.readString(template("v35-preactivation-status.html"))
                + Files.readString(template("v35-operator-guide.html"))
                + Files.readString(template("v35-checksums.html"))
                + Files.readString(template("v35-burn-warning.html"));

        assertTrue(content.contains("V35 activation is scheduled"));
        assertTrue(content.contains("node 1.0.15 Stable / wallet 1.0.2"));
        assertTrue(content.contains("No user action is required yet"));
        assertFalse(content.contains("pending upload"));
        assertFalse(content.contains("GitHub Release upload"));
        assertTrue(content.contains("SHA256"));
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
        assertTrue(content.contains("Recommended node 1.0.15 Stable / wallet 1.0.2"));
        assertTrue(content.contains("Website/operator guide 1.0.7"));
    }

    @Test
    void protocolPaperReplacesLegacyWhitePaperAndLabelsHistoricalGuides() throws IOException {
        String paper = Files.readString(template("white_papper.html"));
        String guide = Files.readString(template("how_to_mining.html"));
        String charter = Files.readString(template("charter_eng.html"));

        assertTrue(paper.contains("V35 pre-activation protocol model"));
        assertTrue(paper.contains("ACTIVATION SCHEDULED: 514291"));
        assertFalse(paper.contains("Long.MAX_VALUE"));
        assertFalse(paper.contains("planning-only"));
        assertTrue(paper.contains("200 seconds"));
        assertTrue(paper.contains("432"));
        assertTrue(paper.contains("8 GOLD"));
        assertTrue(paper.contains("6 GOLD"));
        assertTrue(paper.contains("2 GOLD"));
        assertTrue(paper.contains("0 GOLD"));
        assertTrue(paper.contains("cumulativeWork"));
        assertTrue(paper.contains("No founder burn has occurred"));
        assertTrue(paper.contains("official burn address has not been published"));
        assertFalse(paper.contains("difficulty * 15"));
        assertFalse(paper.contains("0</code> to <code>170"));
        assertTrue(guide.contains("Legacy / pre-V35 mining guide"));
        assertTrue(guide.contains("citu-node-v35-activation-514291-1.0.15.jar"));
        assertTrue(guide.contains("http://127.0.0.1:8082"));
        assertTrue(charter.contains("Legacy / pre-V35 protocol note"));
    }

    @Test
    void currentInstallAndAccountGuidesUseV35ReleaseArtifacts() throws IOException {
        String content = Files.readString(template("how_to_install.html"))
                + Files.readString(template("how_to_install_server.html"))
                + Files.readString(template("how_to_open_an_account.html"))
                + Files.readString(template("how_to_transaction.html"))
                + Files.readString(template("how_mining_pool.html"));

        assertTrue(content.contains("citu-node-v35-activation-514291-1.0.15.jar"));
        assertTrue(content.contains("citu-wallet-v35-activation-514291-1.0.2.jar"));
        assertTrue(content.contains("http://194.87.236.238:82"));
        assertTrue(content.contains("http://127.0.0.1:8083"));
        assertTrue(content.contains("http://127.0.0.1:8082"));
        assertTrue(content.contains("http://127.0.0.1:8082/create-account"));
        assertTrue(content.contains("password/private key"));
        assertTrue(content.contains("BURN_ADDRESS"));
        assertTrue(content.contains("514291"));
        assertTrue(content.contains("No founder burn has occurred"));
        assertTrue(content.contains("Legacy / pre-V35 mining pool information"));
        assertFalse(content.contains("unitedStates-0.0.1-SNAPSHOT.jar"));
        assertFalse(content.contains("0.0.423"));
        assertFalse(content.contains("0.32.216"));
        assertFalse(content.contains("suprtrupr application"));
        assertFalse(content.contains("Update Blockchain as primary current flow"));
    }

    private Path template(String file) {
        return Path.of("src", "main", "resources", "templates", file);
    }
}


