package com.authkit.infrastructure.persistence;

import com.authkit.domain.model.User;
import com.authkit.domain.repository.UserRepository;
import com.authkit.infrastructure.persistence.mapper.UserMapper;
import com.authkit.infrastructure.persistence.repository.SpringUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringUserRepository springRepo;
    private final UserMapper mapper;

    public JpaUserRepository(SpringUserRepository springRepo, UserMapper mapper) {
        this.springRepo = springRepo;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(String id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public User create(User user) {
        return mapper.toDomain(springRepo.save(mapper.toEntity(user)));
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(springRepo.save(mapper.toEntity(user)));
    }
}
