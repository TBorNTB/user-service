package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.UserRepository;
import com.sejong.userservice.exception.UserNotFoundException;
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
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없어요."));
    }

    @Override
    public List<User> findAllUsers() {
        return springDataJpaUserRepository.findAll().stream().map(UserEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public String deleteByUsername(String username) {
        springDataJpaUserRepository.deleteByUsername(username);
        return username;
    }
}
