package com.authkit.infrastructure.persistence.mapper;

import com.authkit.domain.model.User;
import com.authkit.domain.valueobject.Email;
import com.authkit.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper bidireccional dominio ↔ JPA entity.
 * Equivalente al PrismaUserRepository.toDomain() del proyecto original.
 */
@Component
public class UserMapper {

    public User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId(),
                Email.create(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getTotpSecret(),
                entity.isTotpEnabled(),
                entity.getRefreshTokenHash(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .email(user.getEmail().getValue())
                .passwordHash(user.getPasswordHash())
                .totpSecret(user.getTotpSecret())
                .totpEnabled(user.isTotpEnabled())
                .refreshTokenHash(user.getRefreshTokenHash())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
