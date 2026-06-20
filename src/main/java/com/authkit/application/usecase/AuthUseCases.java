package com.authkit.application.usecase;

import org.springframework.stereotype.Service;

/**
 * Facade — agrupa todos los use-cases de auth para inyectarlos juntos en el controller.
 * Cada use-case vive en su propio archivo (regla de Java: una clase pública por archivo).
 */
@Service
public class AuthUseCases {

    public final RegisterUserUseCase    registerUser;
    public final LoginUserUseCase       loginUser;
    public final VerifyTotpLoginUseCase verifyTotpLogin;
    public final RefreshTokenUseCase    refreshToken;
    public final SetupTotpUseCase       setupTotp;
    public final EnableTotpUseCase      enableTotp;
    public final DisableTotpUseCase     disableTotp;
    public final LogoutUseCase          logout;

    public AuthUseCases(RegisterUserUseCase registerUser, LoginUserUseCase loginUser,
                        VerifyTotpLoginUseCase verifyTotpLogin, RefreshTokenUseCase refreshToken,
                        SetupTotpUseCase setupTotp, EnableTotpUseCase enableTotp,
                        DisableTotpUseCase disableTotp, LogoutUseCase logout) {
        this.registerUser    = registerUser;
        this.loginUser       = loginUser;
        this.verifyTotpLogin = verifyTotpLogin;
        this.refreshToken    = refreshToken;
        this.setupTotp       = setupTotp;
        this.enableTotp      = enableTotp;
        this.disableTotp     = disableTotp;
        this.logout          = logout;
    }
}
