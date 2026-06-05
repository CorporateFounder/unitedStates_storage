package com.example.website.citu.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebControllerFastFallbackTest {

    @Test
    void safeNumberParsingUsesFallbackForUnavailableApiValues() {
        assertEquals(0, WebController.safeLong("temporarily unavailable", 0));
        assertEquals(0, WebController.safeInt("temporarily unavailable", 0));
        assertEquals(0.0, WebController.safeDouble("temporarily unavailable", 0.0));
    }

    @Test
    void safeNumberParsingAcceptsPlainAndQuotedNumbers() {
        assertEquals(514291, WebController.safeLong("514291", 0));
        assertEquals(432, WebController.safeInt("\"432\"", 0));
        assertEquals(123.45, WebController.safeDouble("\"123.45\"", 0.0));
    }
}
