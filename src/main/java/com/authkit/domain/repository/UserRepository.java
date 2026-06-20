package com.authkit.domain.repository;

import com.authkit.domain.model.User;

import java.util.Optional;

/**
 * Puerto de dominio: UserRepository
 * El dominio define QUÉ necesita, infrastructure decide CÓMO.
 * Equivalente al user.repository.ts (interface) del proyecto original.
 */
public interface UserRepository {

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    User create(User user);

    User save(User user);
}
