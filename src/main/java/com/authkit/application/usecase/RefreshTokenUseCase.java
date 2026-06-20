package com.authkit.application.usecase;

import com.authkit.application.dto.AuthDtos.RefreshRequest;
import com.authkit.application.dto.AuthDtos.TokensResponse;
import com.authkit.domain.exception.DomainException;
import com.authkit.domain.repository.UserRepository;
import com.authkit.infrastructure.security.JwtTokenServicePort;
import com.authkit.infrastructure.security.JwtTokenServicePort.TokenPayload;
import com.authkit.infrastructure.security.TokenHasherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final JwtTokenServicePort tokenService;
    private final TokenHasherPort tokenHasher;

    public RefreshTokenUseCase(UserRepository userRepository, JwtTokenServicePort tokenService,
                               TokenHasherPort tokenHasher) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.tokenHasher = tokenHasher;
    }

    @Transactional
    public TokensResponse execute(RefreshRequest input) {
        TokenPayload payload = tokenService.verifyRefreshToken(input.refreshToken());

        var user = userRepository.findById(payload.sub())
                .orElseThrow(DomainException.InvalidRefreshToken::new);

        if (user.getRefreshTokenHash() == null ||
                !tokenHasher.matches(input.refreshToken(), user.getRefreshTokenHash())) {
            throw new DomainException.InvalidRefreshToken();
        }

        TokenPayload newPayload = new TokenPayload(user.getId(), user.getEmail().getValue());
        String newAccessToken  = tokenService.signAccessToken(newPayload);
        String newRefreshToken = tokenService.signRefreshToken(newPayload);
        user.setRefreshTokenHash(tokenHasher.hash(newRefreshToken));
        userRepository.save(user);
        return new TokensResponse(newAccessToken, newRefreshToken);
    }
}
