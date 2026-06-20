package com.authkit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA — la capa de infraestructura.
 * NUNCA se expone fuera de infrastructure: el dominio trabaja con User (domain model).
 */
@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "totp_secret")
    private String totpSecret;

    @Column(name = "totp_enabled", nullable = false)
    private boolean totpEnabled;

    @Column(name = "refresh_token_hash")
    private String refreshTokenHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ---- JPA requiere constructor sin args ----
    public UserJpaEntity() {}

    public UserJpaEntity(String id, String email, String passwordHash,
                         String totpSecret, boolean totpEnabled,
                         String refreshTokenHash, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.totpSecret = totpSecret;
        this.totpEnabled = totpEnabled;
        this.refreshTokenHash = refreshTokenHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // ---- Getters ----
    public String getId()               { return id; }
    public String getEmail()            { return email; }
    public String getPasswordHash()     { return passwordHash; }
    public String getTotpSecret()       { return totpSecret; }
    public boolean isTotpEnabled()      { return totpEnabled; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public Instant getCreatedAt()       { return createdAt; }
    public Instant getUpdatedAt()       { return updatedAt; }

    // ---- Setters ----
    public void setId(String id)                       { this.id = id; }
    public void setEmail(String email)                 { this.email = email; }
    public void setPasswordHash(String passwordHash)   { this.passwordHash = passwordHash; }
    public void setTotpSecret(String totpSecret)       { this.totpSecret = totpSecret; }
    public void setTotpEnabled(boolean totpEnabled)    { this.totpEnabled = totpEnabled; }
    public void setRefreshTokenHash(String hash)       { this.refreshTokenHash = hash; }
    public void setCreatedAt(Instant createdAt)        { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt)        { this.updatedAt = updatedAt; }

    // ---- Builder estático ----
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, email, passwordHash, totpSecret, refreshTokenHash;
        private boolean totpEnabled;
        private Instant createdAt, updatedAt;

        public Builder id(String id)                         { this.id = id; return this; }
        public Builder email(String email)                   { this.email = email; return this; }
        public Builder passwordHash(String h)                { this.passwordHash = h; return this; }
        public Builder totpSecret(String s)                  { this.totpSecret = s; return this; }
        public Builder totpEnabled(boolean e)                { this.totpEnabled = e; return this; }
        public Builder refreshTokenHash(String h)            { this.refreshTokenHash = h; return this; }
        public Builder createdAt(Instant t)                  { this.createdAt = t; return this; }
        public Builder updatedAt(Instant t)                  { this.updatedAt = t; return this; }

        public UserJpaEntity build() {
            return new UserJpaEntity(id, email, passwordHash, totpSecret,
                    totpEnabled, refreshTokenHash, createdAt, updatedAt);
        }
    }
}
