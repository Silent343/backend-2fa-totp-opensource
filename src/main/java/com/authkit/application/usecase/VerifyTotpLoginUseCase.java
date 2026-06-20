package com.authkit.application.usecase;

import com.authkit.application.dto.AuthDtos.TokensResponse;
import com.authkit.application.dto.AuthDtos.VerifyTotpLoginRequest;
import com.authkit.domain.exception.DomainException;
import com.authkit.domain.repository.UserRepository;
import com.authkit.infrastructure.security.JwtTokenServicePort;
import com.authkit.infrastructure.security.JwtTokenServicePort.TokenPayload;
import com.authkit.infrastructure.security.TokenHasherPort;
import com.authkit.infrastructure.security.TotpServicePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerifyTotpLoginUseCase {

    private final UserRepository userRepository;
    private final JwtTokenServicePort tokenService;
    private final TotpServicePort totpService;
    private final TokenHasherPort tokenHasher;

    public VerifyTotpLoginUseCase(UserRepository userRepository, JwtTokenServicePort tokenService,
                                  TotpServicePort totpService, TokenHasherPort tokenHasher) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.totpService = totpService;
        this.tokenHasher = tokenHasher;
    }

    @Transactional
    public TokensResponse execute(VerifyTotpLoginRequest input) {
        TokenPayload challengePayload = tokenService.verifyTotpChallengeToken(input.totpChallengeToken());

        var user = userRepository.findById(challengePayload.sub())
                .filter(u -> u.getTotpSecret() != null)
                .orElseThrow(DomainException.UserNotFound::new);

        if (!totpService.verify(input.code(), user.getTotpSecret())) {
            throw new DomainException.InvalidTotpCode();
        }

        TokenPayload payload = new TokenPayload(user.getId(), user.getEmail().getValue());
        String accessToken  = tokenService.signAccessToken(payload);
        String refreshToken = tokenService.signRefreshToken(payload);
        user.setRefreshTokenHash(tokenHasher.hash(refreshToken));
        userRepository.save(user);
        return new TokensResponse(accessToken, refreshToken);
    }
}
