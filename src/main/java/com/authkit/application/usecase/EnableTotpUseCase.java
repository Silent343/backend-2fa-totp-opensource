package com.authkit.application.usecase;

import com.authkit.domain.exception.DomainException;
import com.authkit.domain.repository.UserRepository;
import com.authkit.infrastructure.security.TotpServicePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnableTotpUseCase {

    private final UserRepository userRepository;
    private final TotpServicePort totpService;

    public EnableTotpUseCase(UserRepository userRepository, TotpServicePort totpService) {
        this.userRepository = userRepository;
        this.totpService = totpService;
    }

    @Transactional
    public void execute(String userId, String code) {
        var user = userRepository.findById(userId)
                .orElseThrow(DomainException.UserNotFound::new);

        if (user.getTotpSecret() == null) throw new DomainException.TotpNotInitialized();
        if (!totpService.verify(code, user.getTotpSecret())) throw new DomainException.InvalidTotpCode();

        user.enableTotp(user.getTotpSecret());
        userRepository.save(user);
    }
}
