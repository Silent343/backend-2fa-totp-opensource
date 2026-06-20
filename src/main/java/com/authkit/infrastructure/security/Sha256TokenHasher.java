package com.authkit.infrastructure.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Implementación de TokenHasherPort usando SHA-256.
 * Equivalente a sha256-token-hasher.ts del proyecto original.
 *
 * Se usa SHA-256 y NO bcrypt para refresh tokens porque:
 * - BCrypt trunca el input a 72 bytes (dos JWTs distintos pueden compartir los
 *   primeros 72 bytes del header y matchear por error).
 * - Los refresh tokens ya tienen alta entropía, no necesitan hashing lento.
 */
@Component
public class Sha256TokenHasher implements TokenHasherPort {

    @Override
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    @Override
    public boolean matches(String token, String hash) {
        return hash.equals(this.hash(token));
    }
}
