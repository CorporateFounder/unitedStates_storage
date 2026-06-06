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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LegacyApiContractTest {
    private final List<String> requests = new ArrayList<>();
    private HttpServer server;

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
                .andExpect(content().string(containsString("hash-duplicate")));

        assertTrue(requests.contains("GET /conductorBlock?index=1"));
    }

    @Test
    void blocksPageUsesSubBlocksPostWithStartFinishJson() throws Exception {
        mockMvc.perform(get("/blocks").param("start", "0").param("finish", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hash-range")));

        assertTrue(requests.stream().anyMatch(value -> value.startsWith("POST /sub-blocks ")));
        assertTrue(requests.stream().anyMatch(value -> value.contains("\"start\":0") && value.contains("\"finish\":10")));
    }

    @Test
    void winnersCompatibilityRoutesRenderFromOldEndpoints() throws Exception {
        mockMvc.perform(get("/allwinners"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("winner-address")));

        mockMvc.perform(get("/winnerList"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("winner-list-address")));

        mockMvc.perform(get("/winerList"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("winner-list-address")));

        assertTrue(requests.contains("GET /allwinners"));
        assertTrue(requests.contains("GET /winnerList"));
    }

    private void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().toString();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requests.add(method + " " + path + (body.isBlank() ? "" : " " + body));

        String response = switch (exchange.getRequestURI().getPath()) {
            case "/conductorBlock" -> duplicateIndexBlocks();
            case "/sub-blocks" -> blocksRange();
            case "/allwinners", "/allWinners" -> winner("winner-address");
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
        String renderedIndex = index instanceof String ? "\"" + index + "\"" : String.valueOf(index);
        return "{\"index\":" + renderedIndex
                + ",\"hashBlock\":\"" + hash + "\""
                + ",\"previousHash\":\"prev\""
                + ",\"minerAddress\":\"miner\""
                + ",\"founderAddress\":\"founder\""
                + ",\"randomNumberProof\":7"
                + ",\"minerRewards\":8.5"
                + ",\"hashCompexity\":9"
                + ",\"dtoTransactions\":[]}";
    }

    private String winner(String address) {
        return "[{\"index\":1,\"address\":\"" + address + "\",\"hash\":\"winner-hash\",\"countTransactions\":2,\"staking\":3.5,\"bigRandom\":4,\"diff\":5}]";
    }
}
