package com.authkit.infrastructure.persistence.repository;

import com.authkit.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository — infraestructura pura.
 * Solo ve UserJpaEntity, nunca el User del dominio.
 */
public interface SpringUserRepository extends JpaRepository<UserJpaEntity, String> {
    Optional<UserJpaEntity> findByEmail(String email);
}
