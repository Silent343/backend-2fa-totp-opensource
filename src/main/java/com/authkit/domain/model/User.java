package com.authkit.domain.model;

import com.authkit.domain.valueobject.Email;
import java.time.Instant;

/**
 * Entidad de dominio: User
 * Encapsula el estado y las reglas de negocio del usuario.
 * No conoce Spring, JPA ni JWT — solo reglas del negocio "auth".
 */
public class User {

    private final String id;
    private final Email email;
    private final String passwordHash;
    private String totpSecret;
    private boolean totpEnabled;
    private String refreshTokenHash;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(String id, Email email, String passwordHash,
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

    public static User reconstitute(String id, Email email, String passwordHash,
                                    String totpSecret, boolean totpEnabled,
                                    String refreshTokenHash, Instant createdAt, Instant updatedAt) {
        return new User(id, email, passwordHash, totpSecret, totpEnabled,
                refreshTokenHash, createdAt, updatedAt);
    }

    public static User create(String id, Email email, String passwordHash) {
        Instant now = Instant.now();
        return new User(id, email, passwordHash, null, false, null, now, now);
    }

    // ---- Reglas de negocio ----

    public void enableTotp(String secret) {
        this.totpSecret = secret;
        this.totpEnabled = true;
        this.updatedAt = Instant.now();
    }

    public void disableTotp() {
        this.totpSecret = null;
        this.totpEnabled = false;
        this.updatedAt = Instant.now();
    }

    public void setPendingTotpSecret(String secret) {
        this.totpSecret = secret;
        this.updatedAt = Instant.now();
    }

    public void setRefreshTokenHash(String hash) {
        this.refreshTokenHash = hash;
        this.updatedAt = Instant.now();
    }

    public UserPublicProfile toPublicProfile() {
        return new UserPublicProfile(id, email.getValue(), totpEnabled, createdAt);
    }

    // ---- Getters ----
    public String getId()               { return id; }
    public Email getEmail()             { return email; }
    public String getPasswordHash()     { return passwordHash; }
    public String getTotpSecret()       { return totpSecret; }
    public boolean isTotpEnabled()      { return totpEnabled; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public Instant getCreatedAt()       { return createdAt; }
    public Instant getUpdatedAt()       { return updatedAt; }

    public record UserPublicProfile(String id, String email, boolean totpEnabled, Instant createdAt) {}
}
