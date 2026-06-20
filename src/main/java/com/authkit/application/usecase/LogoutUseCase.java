package com.authkit.application.usecase;

import com.authkit.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

    private final UserRepository userRepository;

    public LogoutUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshTokenHash(null);
            userRepository.save(user);
        });
    }
}
