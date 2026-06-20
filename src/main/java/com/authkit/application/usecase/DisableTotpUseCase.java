package com.authkit.application.usecase;

import com.authkit.domain.exception.DomainException;
import com.authkit.domain.repository.UserRepository;
import com.authkit.infrastructure.security.PasswordHasherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisableTotpUseCase {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasher;

    public DisableTotpUseCase(UserRepository userRepository, PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public void execute(String userId, String password) {
        var user = userRepository.findById(userId)
                .orElseThrow(DomainException.UserNotFound::new);

        if (!passwordHasher.matches(password, user.getPasswordHash()))
            throw new DomainException.InvalidCredentials();

        user.disableTotp();
        userRepository.save(user);
    }
}
