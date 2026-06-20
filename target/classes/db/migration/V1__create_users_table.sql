-- Auth Starter Kit — migración inicial
-- Equivalente al schema.prisma del proyecto original (Node/TypeScript)

CREATE TABLE IF NOT EXISTS users (
    id                  VARCHAR(36)  NOT NULL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    totp_secret         VARCHAR(255),           -- NULL hasta que el usuario inicia el setup de 2FA
    totp_enabled        BOOLEAN      NOT NULL DEFAULT FALSE,
    refresh_token_hash  VARCHAR(255),           -- hash SHA-256 del refresh token vigente
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
