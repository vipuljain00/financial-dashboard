package com.zorvyn.finance.util;

import com.zorvyn.finance.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared helpers for API layers. Use {@link #extractUserIdFromAccessToken(String)} (or Bearer variant)
 * to resolve the authenticated user id from JWT access tokens.
 */
@Component
@RequiredArgsConstructor
public class AppUtility {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Parses a standard Authorization header and returns the {@code userId} claim from the
     * JWT access token. Expects the form {@code Bearer <token>}.
     */
    public Long extractUserIdFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header must be a Bearer token");
        }
        return extractUserIdFromAccessToken(authorizationHeader.substring(7).trim());
    }

    /**
     * Returns the {@code userId} claim from a JWT access token string (no {@code Bearer} prefix).
     */
    public Long extractUserIdFromAccessToken(String jwtAccessToken) {
        return jwtTokenProvider.getUserIdFromToken(jwtAccessToken);
    }
}
