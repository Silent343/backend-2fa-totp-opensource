package com.authkit.infrastructure.security;

/**
 * Puerto: JwtTokenService
 * Equivalente al interface TokenService de security.service.ts
 */
public interface JwtTokenServicePort {

    record TokenPayload(String sub, String email) {}

    String signAccessToken(TokenPayload payload);
    String signRefreshToken(TokenPayload payload);
    String signTotpChallengeToken(TokenPayload payload);

    TokenPayload verifyAccessToken(String token);
    TokenPayload verifyRefreshToken(String token);

    /**
     * Token temporal (5 min) para el paso 2 del login TOTP.
     * Si el token es inválido o expirado, lanza DomainException.InvalidTotpCode.
     */
    TokenPayload verifyTotpChallengeToken(String token);
}
