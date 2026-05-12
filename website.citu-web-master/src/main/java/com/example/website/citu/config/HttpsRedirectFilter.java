//package com.example.website.citu.config;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//
//@Component
//public class HttpsRedirectFilter extends OncePerRequestFilter {
//
//////
//////    @Override
//////    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//////            throws ServletException, IOException {
//////        if (request.getScheme().equals("http")) {
//////            String httpsUrl = "https://" + request.getServerName()
//////                    + request.getRequestURI();
//////            response.sendRedirect(httpsUrl);
//////        } else {
//////            filterChain.doFilter(request, response);
//////        }
//////    }
////}
