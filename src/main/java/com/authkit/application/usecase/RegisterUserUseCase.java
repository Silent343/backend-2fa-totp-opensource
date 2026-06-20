package com.authkit.application.usecase;

import com.authkit.application.dto.AuthDtos.RegisterRequest;
import com.authkit.application.dto.AuthDtos.UserProfileResponse;
import com.authkit.domain.exception.DomainException;
import com.authkit.domain.model.User;
import com.authkit.domain.repository.UserRepository;
import com.authkit.domain.valueobject.Email;
import com.authkit.domain.valueobject.Password;
import com.authkit.infrastructure.security.PasswordHasherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasher;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public UserProfileResponse execute(RegisterRequest input) {
        Email email = Email.create(input.email());
        Password password = Password.create(input.password());

        userRepository.findByEmail(email.getValue()).ifPresent(existing -> {
            throw new DomainException.UserAlreadyExists(email.getValue());
        });

        String passwordHash = passwordHasher.hash(password.getValue());
        User user = User.create(UUID.randomUUID().toString(), email, passwordHash);
        User saved = userRepository.create(user);
        User.UserPublicProfile p = saved.toPublicProfile();
        return new UserProfileResponse(p.id(), p.email(), p.totpEnabled(), p.createdAt().toString());
    }
}
