package com.example.website.citu.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HttpsRedirectFilterTest {

    @Test
    void disabledRedirectAllowsHttpRequestThrough() throws Exception {
        HttpsRedirectFilter filter = new HttpsRedirectFilter(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v35");
        request.setServerName("citucorp.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertFalse(response.isCommitted());
        assertEquals(200, response.getStatus());
    }

    @Test
    void enabledRedirectSendsHttpRequestToHttps() throws Exception {
        HttpsRedirectFilter filter = new HttpsRedirectFilter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v35");
        request.setServerName("citucorp.com");
        request.setQueryString("lang=en");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(302, response.getStatus());
        assertEquals("https://citucorp.com/v35?lang=en", response.getRedirectedUrl());
    }
}
