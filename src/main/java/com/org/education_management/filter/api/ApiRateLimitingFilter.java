package com.org.education_management.filter.api;

import com.org.education_management.util.ApiRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiRateLimitingFilter extends OncePerRequestFilter {

    private final ApiRateLimiter rateLimiter = ApiRateLimiter.getInstance();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();
        String path = request.getRequestURI();

        // Check if the request is allowed by the rate limiter
        if (!rateLimiter.isAllowed(path, clientIp)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return;
        }

        // Proceed with the request if allowed
        filterChain.doFilter(request, response);
    }
}

