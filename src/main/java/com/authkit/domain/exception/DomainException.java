package com.authkit.domain.exception;

/**
 * Excepción base de dominio — equivalente a DomainError en el proyecto original.
 * Todos los errores de negocio extienden esta clase.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    protected DomainException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    // ---- Subclases concretas (una por error de negocio) ----

    public static class UserAlreadyExists extends DomainException {
        public UserAlreadyExists(String email) {
            super("Ya existe una cuenta con el email " + email, "USER_ALREADY_EXISTS", 409);
        }
    }

    public static class InvalidCredentials extends DomainException {
        public InvalidCredentials() {
            super("Email o contraseña incorrectos", "INVALID_CREDENTIALS", 401);
        }
    }

    public static class UserNotFound extends DomainException {
        public UserNotFound() {
            super("Usuario no encontrado", "USER_NOT_FOUND", 404);
        }
    }

    public static class TotpAlreadyEnabled extends DomainException {
        public TotpAlreadyEnabled() {
            super("La autenticación de dos factores ya está activa", "TOTP_ALREADY_ENABLED", 409);
        }
    }

    public static class TotpNotInitialized extends DomainException {
        public TotpNotInitialized() {
            super("Debes iniciar el setup de 2FA antes de confirmarlo", "TOTP_NOT_INITIALIZED", 400);
        }
    }

    public static class InvalidTotpCode extends DomainException {
        public InvalidTotpCode() {
            super("Código de verificación inválido o expirado", "INVALID_TOTP_CODE", 401);
        }
    }

    public static class InvalidRefreshToken extends DomainException {
        public InvalidRefreshToken() {
            super("Refresh token inválido o expirado", "INVALID_REFRESH_TOKEN", 401);
        }
    }
}
