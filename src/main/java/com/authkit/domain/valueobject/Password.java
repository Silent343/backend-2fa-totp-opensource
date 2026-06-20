package com.authkit.domain.valueobject;

/**
 * Value Object: Password (texto plano, solo durante validación de entrada).
 * Centraliza las reglas de negocio de qué es una contraseña aceptable.
 * El hash lo hace infrastructure — el dominio no sabe de BCrypt.
 * Equivalente a password.vo.ts del proyecto original.
 */
public final class Password {

    private final String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password create(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una mayúscula");
        }
        if (!rawPassword.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un número");
        }
        return new Password(rawPassword);
    }

    public String getValue() {
        return value;
    }
}
