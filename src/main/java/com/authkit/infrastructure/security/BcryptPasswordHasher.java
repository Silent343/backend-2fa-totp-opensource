package com.authkit.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Implementación de PasswordHasherPort usando BCrypt.
 * Equivalente a bcrypt-password-hasher.ts del proyecto original.
 */
@Component
public class BcryptPasswordHasher implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    @Override
    public boolean matches(String plainPassword, String hash) {
        return encoder.matches(plainPassword, hash);
    }
}
