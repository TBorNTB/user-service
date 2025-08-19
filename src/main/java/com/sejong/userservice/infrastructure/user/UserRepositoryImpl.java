package com.sejong.userservice.infrastructure.user;

import static com.sejong.userservice.application.common.exception.ExceptionType.NOT_FOUND_USER;

import com.sejong.userservice.application.common.exception.BaseException;
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
        // todo: exception 처리
        UserEntity savedUserEntity = jpaUserRepository.save(UserEntity.from(user));
        savedUserEntity.updateUsername();
        return savedUserEntity.toDomain();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaUserRepository.existsByNickname(nickname);
    }

    @Override
    public User findByUsername(String username) {
        Optional<UserEntity> userEntityOptional = jpaUserRepository.findByUsername(username);

        return userEntityOptional.map(UserEntity::toDomain)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));
    }

    @Override
    public List<User> findAllUsers() {
        return jpaUserRepository.findAll().stream().map(UserEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteByUserNickname(String nickname) {
        jpaUserRepository.deleteByNickname(nickname);
    }

    @Override
    public List<User> findAllByUsernameIn(List<String> usernames) {
        return usernames.stream().map((username) -> jpaUserRepository.findByUsername(username)
                        .orElseThrow(() -> new BaseException(NOT_FOUND_USER)))
                .map(UserEntity::toDomain)
                .toList();
    }

    @Override
    public User findByEmail(String email) {
        UserEntity userEntity = jpaUserRepository.findByEmail(email).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        return userEntity.toDomain();
    }
}
