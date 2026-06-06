package com.example.website.citu.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebsiteApiFallbackRenderingTest {
    private static final String FAILING_API = "http://127.0.0.1:1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionCustomerController customerController;

    @Autowired
    private TransactionSenderController senderController;

    @BeforeEach
    void useFailingApi() {
        ExplorerController.setAddress(FAILING_API);
        WebController.address = FAILING_API;
        customerController.address = FAILING_API;
        senderController.address = FAILING_API;
    }

    @Test
    void conductorRendersWhenNodeApiFails() throws Exception {
        mockMvc.perform(get("/conductor"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Explorer")));
    }

    @Test
    void blocksRendersFallbackRangeWhenNodeApiFails() throws Exception {
        mockMvc.perform(get("/blocks"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("latest size temporarily unavailable; showing fallback range")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("0 to 10")));
    }

    @Test
    void customerRendersWhenNodeApiFails() throws Exception {
        mockMvc.perform(get("/customer").param("pubkey", "test-public-key").param("action", "/send"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("test-public-key")));
    }

    @Test
    void homepageRendersWhenNodeApiFails() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("temporarily unavailable")));
    }

    @Test
    void senderRendersWhenNodeApiFails() throws Exception {
        mockMvc.perform(get("/sender").param("pubkey", "test-public-key").param("action", "/send"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("test-public-key")));
    }

    @Test
    void allwinnersRendersFallbackWhenNodeApiFails() throws Exception {
        mockMvc.perform(get("/allwinners"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("temporarily unavailable")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("citu-section")));
    }

    @Test
    void winnerListAliasesRenderFallbackWhenNodeApiFails() throws Exception {
        mockMvc.perform(get("/winnerList"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("temporarily unavailable")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("winerList"))));

        mockMvc.perform(get("/winerList"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("temporarily unavailable")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("winerList"))));
    }

    @Test
    void v35RendersWhenNodeApiFails() throws Exception {
        mockMvc.perform(get("/v35"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("V35")));
    }
}
