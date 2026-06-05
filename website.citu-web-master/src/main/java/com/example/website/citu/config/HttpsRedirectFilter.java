package com.example.website.citu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HttpsRedirectFilter extends OncePerRequestFilter {
    private final boolean redirectEnabled;

    public HttpsRedirectFilter(@Value("${citu.https.redirect.enabled:false}") boolean redirectEnabled) {
        this.redirectEnabled = redirectEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!redirectEnabled || request.isSecure()) {
            filterChain.doFilter(request, response);
            return;
        }

        String query = request.getQueryString();
        String httpsUrl = "https://" + request.getServerName()
                + request.getRequestURI()
                + (query == null || query.isBlank() ? "" : "?" + query);
        response.sendRedirect(httpsUrl);
    }
}
