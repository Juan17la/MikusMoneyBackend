package com.example.mikusmoneybackend.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.mikusmoneybackend.auth.CookieService;
import com.example.mikusmoneybackend.auth.JwtService;
import com.example.mikusmoneybackend.miku.Miku;
import com.example.mikusmoneybackend.miku.MikuRepository;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter that processes each request to validate JWT tokens.
 * Extracts JWT from cookies and sets the authentication context.
 * 
 * This filter is environment-agnostic and works the same in development and production.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final MikuRepository mikuRepository;

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {

        // Skip if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from cookie
        String token = cookieService.getTokenFromCookies(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validate token and extract user ID
            if (jwtService.isTokenExpired(token)) {
                log.debug("Token expired for request to {}", request.getRequestURI());
                cookieService.clearAuthCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtService.getUserIdFromToken(token);

            // Load user from database
            Miku miku = mikuRepository.findById(userId).orElse(null);

            if (miku != null && jwtService.isTokenValid(token, userId)) {
                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        miku,
                        null,
                        Collections.emptyList()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("Authenticated user {} for request to {}", miku.getId(), request.getRequestURI());
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            cookieService.clearAuthCookie(response);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filter for public endpoints
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs");
    }
}
