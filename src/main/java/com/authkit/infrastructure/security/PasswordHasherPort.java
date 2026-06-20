package com.authkit.infrastructure.security;

/**
 * Puerto: PasswordHasher
 * Equivalente al interface PasswordHasher de security.service.ts
 */
public interface PasswordHasherPort {
    String hash(String plainPassword);
    boolean matches(String plainPassword, String hash);
}
