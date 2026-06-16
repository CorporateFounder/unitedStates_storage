package com.example.website.citu.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LegacyApiContractTest {
    private final List<String> requests = new ArrayList<>();
    private HttpServer server;
    private boolean sizeUnavailable;
    private boolean tournamentEmpty;
    private boolean currentCandidatesEmpty;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionCustomerController customerController;

    @Autowired
    private TransactionSenderController senderController;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        ExplorerController.setAddress(baseUrl);
        WebController.address = baseUrl;
        customerController.address = baseUrl;
        senderController.address = baseUrl;
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void conductorLookupUsesConductorBlockByIndexAndHandlesDuplicateIndexArray() throws Exception {
        mockMvc.perform(get("/conductor/lookup").param("index", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hash-one")))
                .andExpect(content().string(containsString("hash-duplicate")))
                .andExpect(content().string(containsString("Duplicate block indexes can exist")))
                .andExpect(content().string(containsString("/detail-transactions/1")));

        assertTrue(requests.contains("GET /conductorBlock?index=1"));
    }

    @Test
    void conductorRendersSearchFormsAndRecentBlockPreviewWithDetailsLinks() throws Exception {
        mockMvc.perform(get("/conductor"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enter index block or sign transaction")))
                .andExpect(content().string(containsString("Address history")))
                .andExpect(content().string(containsString("Latest block preview")))
                .andExpect(content().string(containsString("hash-range")))
                .andExpect(content().string(containsString("/detail-transactions/0")));

        assertTrue(requests.contains("GET /size"));
        assertTrue(requests.stream().anyMatch(value -> value.startsWith("POST /sub-blocks ")
                && value.contains("\"start\":110")
                && value.contains("\"finish\":120")));
    }

    @Test
    void blocksPageUsesSubBlocksPostWithStartFinishJson() throws Exception {
        mockMvc.perform(get("/blocks").param("start", "0").param("finish", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hash-range")));

        assertFalse(requests.contains("GET /size"));
        assertTrue(requests.stream().anyMatch(value -> value.startsWith("POST /sub-blocks ")));
        assertTrue(requests.stream().anyMatch(value -> value.contains("\"start\":0") && value.contains("\"finish\":10")));
    }

    @Test
    void blocksPageDefaultsToLatestRangeFromSize() throws Exception {
        mockMvc.perform(get("/blocks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("110 to 120")))
                .andExpect(content().string(containsString("hash-range")))
                .andExpect(content().string(containsString("/detail-transactions/0")))
                .andExpect(content().string(not(containsString(">null<"))));

        assertTrue(requests.contains("GET /size"));
        assertTrue(requests.stream().anyMatch(value -> value.startsWith("POST /sub-blocks ")
                && value.contains("\"start\":110")
                && value.contains("\"finish\":120")));
    }

    @Test
    void blocksPageFallsBackToZeroTenWhenSizeUnavailable() throws Exception {
        sizeUnavailable = true;

        mockMvc.perform(get("/blocks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("latest size temporarily unavailable; showing fallback range")))
                .andExpect(content().string(containsString("0 to 10")));

        assertTrue(requests.contains("GET /size"));
        assertTrue(requests.stream().anyMatch(value -> value.startsWith("POST /sub-blocks ")
                && value.contains("\"start\":0")
                && value.contains("\"finish\":10")));
    }

    @Test
    void blocksManualPostStillUsesSubmittedRange() throws Exception {
        mockMvc.perform(post("/blocks")
                        .param("start", "40")
                        .param("finish", "45"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("40 to 45")));

        assertFalse(requests.contains("GET /size"));
        assertTrue(requests.stream().anyMatch(value -> value.startsWith("POST /sub-blocks ")
                && value.contains("\"start\":40")
                && value.contains("\"finish\":45")));
    }

    @Test
    void detailRoutesFromBlocksAndConductorRenderBlockDetails() throws Exception {
        mockMvc.perform(get("/detail-transactions/0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Block details")))
                .andExpect(content().string(containsString("hash-one")))
                .andExpect(content().string(containsString("Transactions")))
                .andExpect(content().string(not(containsString(">null<"))));

        mockMvc.perform(get("/detail-block/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hash-duplicate")))
                .andExpect(content().string(containsString("Duplicate block indexes can exist")));
    }

    @Test
    void detailTransactionsWithoutParamsShowsFriendlyPage() throws Exception {
        mockMvc.perform(get("/detail-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Select a block or transaction from Explorer.")))
                .andExpect(content().string(not(containsString(">null<"))));
    }

    @Test
    void winnersCompatibilityRoutesRenderFromTournamentEndpoints() throws Exception {
        mockMvc.perform(get("/allwinners"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("winner-address")))
                .andExpect(content().string(containsString("citu-section")));

        mockMvc.perform(get("/winnerList"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("winner-list-address")))
                .andExpect(content().string(containsString("citu-section")));

        mockMvc.perform(get("/winerList"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("winner-list-address")))
                .andExpect(content().string(containsString("citu-section")));

        assertTrue(requests.contains("GET /lastTournamentCandidates"));
        assertTrue(requests.contains("GET /lastTournament"));
        assertTrue(requests.contains("GET /currentCandidates"));
    }

    @Test
    void winnersPagesTreatEmptyArraysAsValidEmptyState() throws Exception {
        tournamentEmpty = true;
        mockMvc.perform(get("/allwinners"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No tournament snapshot yet")))
                .andExpect(content().string(not(containsString("temporarily unavailable"))));

        currentCandidatesEmpty = true;
        requests.clear();
        mockMvc.perform(get("/winnerList"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No current candidates right now")))
                .andExpect(content().string(not(containsString("temporarily unavailable"))));
    }

    @Test
    void senderRoutesUseSenderTransactionsEndpointWithAddressFromTo() throws Exception {
        mockMvc.perform(get("/sender")
                        .param("pubkey", "sender-address")
                        .param("action", "/send"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sender-address")))
                .andExpect(content().string(containsString("LAW_HASH")));

        mockMvc.perform(get("/sender")
                        .param("address", "sender-address")
                        .param("from", "0")
                        .param("to", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sender transactions")));

        mockMvc.perform(get("/send")
                        .param("pubkey", "sender-address"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sender transactions")));

        assertTrue(requests.contains("GET /senderTransactions?address=sender-address&from=0&to=10"));
        assertFalse(requests.stream().anyMatch(request -> request.contains("/customerTransactions?address=sender-address")));
    }

    @Test
    void emptySenderResultRendersNotFoundSafely() throws Exception {
        mockMvc.perform(get("/sender")
                        .param("address", "empty-sender")
                        .param("from", "0")
                        .param("to", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("not found")));
    }

    @Test
    void customerRoutesUseCustomerTransactionsEndpointWithAddressFromTo() throws Exception {
        mockMvc.perform(get("/customer")
                        .param("pubkey", "customer-address")
                        .param("action", "/send"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("customer-address")));

        mockMvc.perform(get("/customer")
                        .param("address", "customer-address")
                        .param("from", "0")
                        .param("to", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Customer transactions")));

        assertTrue(requests.contains("GET /customerTransactions?address=customer-address&from=0&to=10"));
    }

    @Test
    void publicNavigationHasCleanUniqueRoutes() throws Exception {
        String header = Files.readString(Path.of("src", "main", "resources", "templates", "blocks", "header.html"));

        assertTrue(header.contains("href=\"/\""));
        assertTrue(header.contains("href=\"/conductor\""));
        assertTrue(header.contains("href=\"/blocks\""));
        assertTrue(header.contains("href=\"/winners\""));
        assertTrue(header.contains("href=\"/wallet_and_node_url\""));
        assertTrue(header.contains("href=\"/white_papper\""));
        assertTrue(header.contains("href=\"/charter\""));
        assertTrue(header.contains("href=\"/v35\""));
        assertFalse(header.contains("winerList"));
        assertFalse(header.contains("winnerList"));
        assertTrue(header.indexOf("href=\"/conductor\"") == header.lastIndexOf("href=\"/conductor\""));
        assertTrue(header.indexOf("href=\"/blocks\"") == header.lastIndexOf("href=\"/blocks\""));
        assertTrue(header.indexOf("href=\"/winners\"") == header.lastIndexOf("href=\"/winners\""));
    }

    @Test
    void winnersAndTransactionPagesUseDarkSharedLayout() throws Exception {
        String winners = Files.readString(Path.of("src", "main", "resources", "templates", "winners.html"));
        String sender = Files.readString(Path.of("src", "main", "resources", "templates", "transactionsSender.html"));

        assertTrue(winners.contains("body class=\"v35-public\""));
        assertTrue(winners.contains("blocks/header :: header"));
        assertTrue(winners.contains("citu-section"));
        assertTrue(winners.contains("citu-winner-card"));
        assertFalse(winners.contains("alert alert-info"));
        assertFalse(winners.contains("winerList"));
        String blocks = Files.readString(Path.of("src", "main", "resources", "templates", "blocks.html"));
        assertTrue(blocks.contains("body class=\"v35-public\""));
        assertTrue(blocks.contains("citu-block-card"));
        assertTrue(blocks.contains("/detail-transactions/"));
        assertFalse(blocks.contains("alert alert-info"));
        String conductor = Files.readString(Path.of("src", "main", "resources", "templates", "conductor.html"));
        assertTrue(conductor.contains("Latest block preview"));
        assertTrue(conductor.contains("/detail-transactions/"));
        String details = Files.readString(Path.of("src", "main", "resources", "templates", "detail-transactions.html"));
        assertTrue(details.contains("Select a block or transaction from Explorer."));
        assertTrue(details.contains("not available"));
        assertTrue(sender.contains("body class=\"v35-public\""));
        assertTrue(sender.contains("citu-transaction-card"));
        assertTrue(sender.contains("Law hash"));
        assertTrue(sender.contains("Law package"));
    }

    private void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().toString();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requests.add(method + " " + path + (body.isBlank() ? "" : " " + body));

        String response = switch (exchange.getRequestURI().getPath()) {
            case "/conductorBlock" -> duplicateIndexBlocks();
            case "/size" -> sizeUnavailable ? "" : "120";
            case "/sub-blocks" -> blocksRange();
            case "/senderTransactions" -> senderTransactions(exchange.getRequestURI().getRawQuery());
            case "/customerTransactions" -> customerTransactions(exchange.getRequestURI().getRawQuery());
            case "/lastTournamentCandidates", "/allwinners", "/allWinners" -> tournamentEmpty ? "[]" : winner("winner-address");
            case "/lastTournament" -> "{\"candidateCount\":1,\"selectedWinnerMinerAddress\":\"winner-address\",\"blockIndex\":1,\"selectionReason\":\"test\",\"selectionStage\":\"test\",\"createdAt\":123}";
            case "/currentCandidates" -> currentCandidatesEmpty ? "[]" : "[" + blockJson(2, "winner-list-hash", "winner-list-address") + "]";
            case "/winnerList", "/winerList" -> winner("winner-list-address");
            case "/bigRandomWiner", "/bigRandomWinner" -> winner("big-random-address");
            default -> "[]";
        };
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String duplicateIndexBlocks() {
        return "[" + blockJson(1, "hash-one") + "," + blockJson("1", "hash-duplicate") + "]";
    }

    private String blocksRange() {
        return "[" + blockJson(0, "hash-range") + "]";
    }

    private String blockJson(Object index, String hash) {
        return blockJson(index, hash, "miner");
    }

    private String blockJson(Object index, String hash, String minerAddress) {
        String renderedIndex = index instanceof String ? "\"" + index + "\"" : String.valueOf(index);
        return "{\"index\":" + renderedIndex
                + ",\"hashBlock\":\"" + hash + "\""
                + ",\"previousHash\":\"prev\""
                + ",\"minerAddress\":\"" + minerAddress + "\""
                + ",\"founderAddress\":\"founder\""
                + ",\"randomNumberProof\":7"
                + ",\"minerRewards\":8.5"
                + ",\"hashCompexity\":9"
                + ",\"dtoTransactions\":[]}";
    }

    private String winner(String address) {
        return "[{\"index\":1,\"address\":\"" + address + "\",\"hash\":\"winner-hash\",\"countTransactions\":2,\"staking\":3.5,\"bigRandom\":4,\"diff\":5}]";
    }

    private String senderTransactions(String query) {
        if (query != null && query.contains("empty-sender")) {
            return "[]";
        }
        return "[" + transaction("sender-address", "recipient-address") + "]";
    }

    private String customerTransactions(String query) {
        return "[" + transaction("sender-address", "customer-address") + "]";
    }

    private String transaction(String sender, String customer) {
        return "{\"sender\":\"" + sender + "\","
                + "\"customer\":\"" + customer + "\","
                + "\"digitalDollar\":1.0E-4,"
                + "\"digitalStockBalance\":2.5,"
                + "\"bonusForMiner\":0.01,"
                + "\"voteEnum\":\"YES\","
                + "\"signStr\":\"SIGNATURE\","
                + "\"laws\":{\"packetLawName\":\"LAW_PACKET\",\"hashLaw\":\"LAW_HASH\",\"laws\":[\"line\"]}}";
    }
}
