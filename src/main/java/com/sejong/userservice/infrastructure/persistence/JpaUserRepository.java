package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataJpaUserRepository springDataJpaUserRepository;

    public JpaUserRepository(SpringDataJpaUserRepository springDataJpaUserRepository) {
        this.springDataJpaUserRepository = springDataJpaUserRepository;
    }

    @Override
    public User save(User user) {
        UserEntity savedUserEntity = springDataJpaUserRepository.save(UserEntity.from(user));
        return savedUserEntity.toDomain();
    }

    @Override
    public boolean existsByUsername(String loginId) {
        return springDataJpaUserRepository.existsByUsername(loginId);
    }

    @Override
    public User findByUsername(String username) {
        Optional<UserEntity> userEntityOptional = springDataJpaUserRepository.findByUsername(username);

        return userEntityOptional.map(UserEntity::toDomain)
                .orElse(null);
    }

    @Override
    public List<User> findAllUsers() {
        return springDataJpaUserRepository.findAll().stream().map(UserEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        springDataJpaUserRepository.deleteByUsername(username);
    }
}
