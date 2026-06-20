package com.authkit.infrastructure.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Implementación de TotpServicePort usando GoogleAuthenticator.
 * Equivalente a totp.service.ts del proyecto original (que usaba otplib).
 */
@Service
public class TotpService implements TotpServicePort {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final String issuer;

    public TotpService(@Value("${totp.issuer:Auth Starter Kit}") String issuer) {
        this.issuer = issuer;
    }

    @Override
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * Genera la URI otpauth:// estándar para escanear con Google Authenticator / Authy.
     * Formato: otpauth://totp/{issuer}:{email}?secret={secret}&issuer={issuer}
     */
    @Override
    public String generateUri(String secret, String email) {
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encodedEmail  = URLEncoder.encode(email, StandardCharsets.UTF_8);
        return "otpauth://totp/" + encodedIssuer + ":" + encodedEmail
                + "?secret=" + secret
                + "&issuer=" + encodedIssuer;
    }

    @Override
    public boolean verify(String code, String secret) {
        try {
            int codeInt = Integer.parseInt(code.trim());
            return gAuth.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
