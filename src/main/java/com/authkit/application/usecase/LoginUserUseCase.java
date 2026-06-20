package com.authkit.application.usecase;

import com.authkit.application.dto.AuthDtos.LoginRequest;
import com.authkit.application.dto.AuthDtos.LoginResponse;
import com.authkit.domain.exception.DomainException;
import com.authkit.domain.repository.UserRepository;
import com.authkit.infrastructure.security.JwtTokenServicePort;
import com.authkit.infrastructure.security.JwtTokenServicePort.TokenPayload;
import com.authkit.infrastructure.security.PasswordHasherPort;
import com.authkit.infrastructure.security.TokenHasherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasher;
    private final JwtTokenServicePort tokenService;
    private final TokenHasherPort tokenHasher;

    public LoginUserUseCase(UserRepository userRepository, PasswordHasherPort passwordHasher,
                            JwtTokenServicePort tokenService, TokenHasherPort tokenHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.tokenHasher = tokenHasher;
    }

    @Transactional
    public LoginResponse execute(LoginRequest input) {
        var user = userRepository.findByEmail(input.email().trim().toLowerCase())
                .orElseThrow(DomainException.InvalidCredentials::new);

        if (!passwordHasher.matches(input.password(), user.getPasswordHash())) {
            throw new DomainException.InvalidCredentials();
        }

        TokenPayload payload = new TokenPayload(user.getId(), user.getEmail().getValue());

        if (user.isTotpEnabled()) {
            return LoginResponse.withTotpChallenge(tokenService.signTotpChallengeToken(payload));
        }

        String accessToken  = tokenService.signAccessToken(payload);
        String refreshToken = tokenService.signRefreshToken(payload);
        user.setRefreshTokenHash(tokenHasher.hash(refreshToken));
        userRepository.save(user);
        return LoginResponse.withTokens(accessToken, refreshToken);
    }
}
