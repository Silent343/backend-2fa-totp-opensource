package com.authkit.infrastructure.security;

import com.authkit.domain.exception.DomainException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Implementación de JwtTokenServicePort usando JJWT.
 * Equivalente a jwt.service.ts del proyecto original.
 *
 * Cada token lleva un jti (JWT ID) único para evitar que dos tokens firmados
 * con el mismo payload en el mismo segundo sean idénticos, rompiendo la rotación.
 */
@Service
public class JwtTokenService implements JwtTokenServicePort {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final SecretKey totpChallengeKey;

    private final long accessExpiresInMs;
    private final long refreshExpiresInMs;
    private final long totpChallengeExpiresInMs;

    public JwtTokenService(
            @Value("${jwt.access-secret}")           String accessSecret,
            @Value("${jwt.refresh-secret}")          String refreshSecret,
            @Value("${jwt.totp-challenge-secret}")   String totpChallengeSecret,
            @Value("${jwt.access-expires-in:900}")         long accessExpiresIn,
            @Value("${jwt.refresh-expires-in:604800}")     long refreshExpiresIn,
            @Value("${jwt.totp-challenge-expires-in:300}") long totpChallengeExpiresIn
    ) {
        this.accessKey              = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey             = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.totpChallengeKey       = Keys.hmacShaKeyFor(totpChallengeSecret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiresInMs       = accessExpiresIn * 1000L;
        this.refreshExpiresInMs      = refreshExpiresIn * 1000L;
        this.totpChallengeExpiresInMs = totpChallengeExpiresIn * 1000L;
    }

    @Override
    public String signAccessToken(TokenPayload payload) {
        return buildToken(payload, accessKey, accessExpiresInMs);
    }

    @Override
    public String signRefreshToken(TokenPayload payload) {
        return buildToken(payload, refreshKey, refreshExpiresInMs);
    }

    @Override
    public String signTotpChallengeToken(TokenPayload payload) {
        return buildToken(payload, totpChallengeKey, totpChallengeExpiresInMs);
    }

    @Override
    public TokenPayload verifyAccessToken(String token) {
        return parseToken(token, accessKey);
    }

    @Override
    public TokenPayload verifyRefreshToken(String token) {
        try {
            return parseToken(token, refreshKey);
        } catch (JwtException e) {
            throw new DomainException.InvalidRefreshToken();
        }
    }

    @Override
    public TokenPayload verifyTotpChallengeToken(String token) {
        try {
            return parseToken(token, totpChallengeKey);
        } catch (JwtException e) {
            throw new DomainException.InvalidTotpCode();
        }
    }

    // ---- helpers privados ----

    private String buildToken(TokenPayload payload, SecretKey key, long expiresInMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(payload.sub())
                .claim("email", payload.email())
                .id(UUID.randomUUID().toString())   // jti único por token
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiresInMs))
                .signWith(key)
                .compact();
    }

    private TokenPayload parseToken(String token, SecretKey key) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new TokenPayload(claims.getSubject(), claims.get("email", String.class));
    }
}
