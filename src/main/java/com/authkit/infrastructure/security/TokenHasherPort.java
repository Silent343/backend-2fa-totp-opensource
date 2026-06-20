package com.authkit.infrastructure.security;

/**
 * Puerto: TokenHasher (SHA-256 rápido, NO bcrypt).
 * Igual que bcrypt, pero los refresh tokens ya son de alta entropía
 * y bcrypt trunca a 72 bytes — SHA-256 es el approach correcto aquí.
 * Equivalente al interface TokenHasher de security.service.ts
 */
public interface TokenHasherPort {
    String hash(String token);
    boolean matches(String token, String hash);
}
