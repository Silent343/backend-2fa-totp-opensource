package com.authkit.infrastructure.web.controller;

import com.authkit.application.dto.AuthDtos.*;
import com.authkit.application.usecase.AuthUseCases;
import com.authkit.domain.exception.DomainException;
import com.authkit.domain.model.User;
import com.authkit.domain.repository.UserRepository;
import com.authkit.infrastructure.web.filter.JwtAuthFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Registro, login y gestión de 2FA")
public class AuthController {

    private final AuthUseCases useCases;
    private final UserRepository userRepository;

    public AuthController(AuthUseCases useCases, UserRepository userRepository) {
        this.useCases = useCases;
        this.userRepository = userRepository;
    }

    // ── Públicas ──────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email ya registrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserProfileResponse register(@Valid @RequestBody RegisterRequest req) {
        return useCases.registerUser.execute(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
               description = "Si el usuario tiene 2FA activo devuelve `totpRequired: true` y un `totpChallengeToken`. Úsalo en `POST /auth/login/totp`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(useCases.loginUser.execute(req));
    }

    @PostMapping("/login/totp")
    @Operation(summary = "Paso 2: verificar código TOTP")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verificación exitosa",
            content = @Content(schema = @Schema(implementation = TokensResponse.class))),
        @ApiResponse(responseCode = "401", description = "Código inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokensResponse> verifyTotpLogin(@Valid @RequestBody VerifyTotpLoginRequest req) {
        return ResponseEntity.ok(useCases.verifyTotpLogin.execute(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens renovados",
            content = @Content(schema = @Schema(implementation = TokensResponse.class))),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokensResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(useCases.refreshToken.execute(req));
    }

    // ── Protegidas ────────────────────────────────────────────────────────────

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Perfil del usuario autenticado 🔒")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil del usuario",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido o ausente")
    })
    public ResponseEntity<UserProfileResponse> me(HttpServletRequest request) {
        String userId = getUserId(request);
        User user = userRepository.findById(userId)
                .orElseThrow(DomainException.UserNotFound::new);
        User.UserPublicProfile p = user.toPublicProfile();
        return ResponseEntity.ok(new UserProfileResponse(p.id(), p.email(), p.totpEnabled(), p.createdAt().toString()));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesión 🔒")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    public void logout(HttpServletRequest request) {
        useCases.logout.execute(getUserId(request));
    }

    @PostMapping("/totp/setup")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Iniciar setup de 2FA 🔒",
               description = "Genera el secret y el QR code. Escanéalo y confirma con `POST /auth/totp/enable`.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "QR y secret generados",
            content = @Content(schema = @Schema(implementation = SetupTotpResponse.class))),
        @ApiResponse(responseCode = "409", description = "2FA ya está activo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SetupTotpResponse> setupTotp(HttpServletRequest request) {
        return ResponseEntity.ok(useCases.setupTotp.execute(getUserId(request)));
    }

    @PostMapping("/totp/enable")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Confirmar activación de 2FA 🔒")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "2FA activado",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Código inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> enableTotp(HttpServletRequest request,
                                                       @Valid @RequestBody EnableTotpRequest req) {
        useCases.enableTotp.execute(getUserId(request), req.code());
        return ResponseEntity.ok(new MessageResponse("2FA activado correctamente"));
    }

    @PostMapping("/totp/disable")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desactivar 2FA 🔒")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "2FA desactivado",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Contraseña incorrecta",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> disableTotp(HttpServletRequest request,
                                                        @Valid @RequestBody DisableTotpRequest req) {
        useCases.disableTotp.execute(getUserId(request), req.password());
        return ResponseEntity.ok(new MessageResponse("2FA desactivado correctamente"));
    }

    private String getUserId(HttpServletRequest request) {
        return (String) request.getAttribute(JwtAuthFilter.USER_ID_ATTR);
    }
}
