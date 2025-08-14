package com.sejong.userservice.infrastructure.user;

import com.sejong.userservice.application.common.exception.UserNotFoundException;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        UserEntity savedUserEntity = jpaUserRepository.save(UserEntity.from(user));
        return savedUserEntity.toDomain();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaUserRepository.existsByNickname(nickname);
    }

    @Override
    public User findByUsername(String username) {
        Optional<UserEntity> userEntityOptional = jpaUserRepository.findByNickname(username);

        return userEntityOptional.map(UserEntity::toDomain)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없어요."));
    }

    @Override
    public List<User> findAllUsers() {
        return jpaUserRepository.findAll().stream().map(UserEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public String deleteByUserNickname(String nickname) {
        jpaUserRepository.deleteByNickname(nickname);
        return nickname;
    }

    @Override
    public List<User> findAllByUsernameIn(List<String> nicknames) {
        return nicknames.stream().map((nickname) -> jpaUserRepository.findByNickname(nickname)
                        .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없어요.")))
                .map(UserEntity::toDomain)
                .toList();
    }
}
