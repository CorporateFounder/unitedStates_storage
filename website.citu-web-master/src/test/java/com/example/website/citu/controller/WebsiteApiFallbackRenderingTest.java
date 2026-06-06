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
                .andExpect(content().string(org.hamcrest.Matchers.containsString("temporarily unavailable")));
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
}
