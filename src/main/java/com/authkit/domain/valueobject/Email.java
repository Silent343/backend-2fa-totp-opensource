package com.authkit.domain.valueobject;

import java.util.regex.Pattern;

/**
 * Value Object: Email
 * Garantiza que cualquier email en el dominio sea válido.
 * Inmutable por diseño — equivalente a email.vo.ts del proyecto original.
 */
public final class Email {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email create(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        String normalized = rawEmail.trim().toLowerCase();
        if (!EMAIL_REGEX.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Email inválido: \"" + rawEmail + "\"");
        }
        return new Email(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
