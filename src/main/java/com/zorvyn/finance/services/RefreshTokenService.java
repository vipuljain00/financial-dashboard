package com.zorvyn.finance.services;

import com.zorvyn.finance.entities.RefreshToken;
import com.zorvyn.finance.entities.User;
import com.zorvyn.finance.exceptions.UnauthorizedException;
import com.zorvyn.finance.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Opaque refresh tokens persisted server-side (hash only). Ensures repository {@code save}
 * runs inside a transaction for login and refresh rotation.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration.ms:604800000}")
    private long refreshExpirationMs;

    @Transactional
    public String createRefreshToken(User user) {
        String raw = generateRawToken();
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(sha256Hex(raw));
        entity.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plus(Duration.ofMillis(refreshExpirationMs)));
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);
        return raw;
    }

    /**
     * Validates the raw refresh token, revokes it (rotation), and returns the associated user.
     */
    @Transactional
    public User consumeAndValidateRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Missing refresh token");
        }
        String hash = sha256Hex(rawToken.trim());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (stored.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new UnauthorizedException("Refresh token expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return stored.getUser();
    }

    private static String generateRawToken() {
        byte[] buf = new byte[32];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
