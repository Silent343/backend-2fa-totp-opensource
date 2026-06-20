package com.authkit.infrastructure.security;

/**
 * Puerto: TotpService
 * Equivalente al interface TotpService de security.service.ts
 */
public interface TotpServicePort {
    String generateSecret();
    /** Genera la URI otpauth:// para el QR de Google Authenticator / Authy */
    String generateUri(String secret, String email);
    boolean verify(String code, String secret);
}
