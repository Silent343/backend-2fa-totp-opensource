package com.authkit.application.usecase;

import com.authkit.application.dto.AuthDtos.SetupTotpResponse;
import com.authkit.domain.exception.DomainException;
import com.authkit.domain.repository.UserRepository;
import com.authkit.infrastructure.security.QrCodeGeneratorPort;
import com.authkit.infrastructure.security.TotpServicePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetupTotpUseCase {

    private final UserRepository userRepository;
    private final TotpServicePort totpService;
    private final QrCodeGeneratorPort qrCodeGenerator;

    public SetupTotpUseCase(UserRepository userRepository, TotpServicePort totpService,
                            QrCodeGeneratorPort qrCodeGenerator) {
        this.userRepository = userRepository;
        this.totpService = totpService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @Transactional
    public SetupTotpResponse execute(String userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(DomainException.UserNotFound::new);

        if (user.isTotpEnabled()) throw new DomainException.TotpAlreadyEnabled();

        String secret    = totpService.generateSecret();
        String uri       = totpService.generateUri(secret, user.getEmail().getValue());
        String qrDataUrl = qrCodeGenerator.toDataUrl(uri);

        user.setPendingTotpSecret(secret);
        userRepository.save(user);
        return new SetupTotpResponse(secret, uri, qrDataUrl);
    }
}
