package com.authkit.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTOs de la capa de aplicación.
 * Equivalente a auth.dtos.ts del proyecto original.
 * Se usan records de Java para garantizar inmutabilidad.
 */
public final class AuthDtos {

    private AuthDtos() {}

    // ======================== Requests ========================

    @Schema(description = "Datos para registrar un nuevo usuario")
    public record RegisterRequest(
            @Schema(example = "usuario@ejemplo.com")
            @Email(message = "El email no es válido")
            @NotBlank(message = "El email es obligatorio")
            String email,

            @Schema(example = "MiPassword1", description = "Mínimo 8 caracteres, una mayúscula y un número")
            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
            String password
    ) {}

    @Schema(description = "Credenciales para iniciar sesión")
    public record LoginRequest(
            @Schema(example = "usuario@ejemplo.com")
            @Email(message = "El email no es válido")
            @NotBlank(message = "El email es obligatorio")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            String password
    ) {}

    @Schema(description = "Token de challenge TOTP + código de 6 dígitos del authenticator")
    public record VerifyTotpLoginRequest(
            @Schema(description = "Token recibido en el paso 1 del login cuando totpRequired=true")
            @NotBlank(message = "El totpChallengeToken es obligatorio")
            String totpChallengeToken,

            @Schema(example = "123456", description = "Código de 6 dígitos del authenticator")
            @NotBlank(message = "El código TOTP es obligatorio")
            String code
    ) {}

    @Schema(description = "Refresh token para obtener nuevos access tokens")
    public record RefreshRequest(
            @NotBlank(message = "El refreshToken es obligatorio")
            String refreshToken
    ) {}

    @Schema(description = "Código TOTP de 6 dígitos para confirmar la activación del 2FA")
    public record EnableTotpRequest(
            @Schema(example = "123456")
            @NotBlank(message = "El código TOTP es obligatorio")
            String code
    ) {}

    @Schema(description = "Contraseña actual para desactivar el 2FA")
    public record DisableTotpRequest(
            @NotBlank(message = "La contraseña es obligatoria")
            String password
    ) {}

    // ======================== Responses ========================

    @Schema(description = "Perfil público del usuario (sin campos sensibles)")
    public record UserProfileResponse(
            String id,
            String email,
            boolean totpEnabled,
            String createdAt
    ) {}

    /**
     * Respuesta del login — puede ser tokens directos o un challenge TOTP.
     * Equivalente al LoginUserOutput union type del original.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Resultado del login. Si totpRequired=true, usar totpChallengeToken para el paso 2")
    public record LoginResponse(
            @Schema(description = "true si el usuario tiene 2FA activo y debe completar el paso 2")
            boolean totpRequired,

            @Schema(description = "JWT de acceso de corta duración (15 min). Presente solo si totpRequired=false")
            String accessToken,

            @Schema(description = "JWT de refresco de larga duración (7 días). Presente solo si totpRequired=false")
            String refreshToken,

            @Schema(description = "Token temporal para el paso 2 del login TOTP (5 min). Presente solo si totpRequired=true")
            String totpChallengeToken
    ) {
        public static LoginResponse withTokens(String accessToken, String refreshToken) {
            return new LoginResponse(false, accessToken, refreshToken, null);
        }

        public static LoginResponse withTotpChallenge(String challengeToken) {
            return new LoginResponse(true, null, null, challengeToken);
        }
    }

    @Schema(description = "Par de tokens JWT")
    public record TokensResponse(
            @Schema(description = "JWT de acceso (15 min)")
            String accessToken,

            @Schema(description = "JWT de refresco (7 días)")
            String refreshToken
    ) {}

    @Schema(description = "Resultado del setup de TOTP — datos para configurar el authenticator")
    public record SetupTotpResponse(
            @Schema(description = "Secret TOTP en Base32 para entrada manual en el authenticator")
            String secret,

            @Schema(description = "URI otpauth:// estándar")
            String otpauthUri,

            @Schema(description = "QR Code como data URL (base64 PNG) para mostrar en el frontend")
            String qrCodeDataUrl
    ) {}

    @Schema(description = "Mensaje de confirmación genérico")
    public record MessageResponse(String message) {}

    @Schema(description = "Respuesta de error estándar")
    public record ErrorResponse(String code, String message) {}
}
