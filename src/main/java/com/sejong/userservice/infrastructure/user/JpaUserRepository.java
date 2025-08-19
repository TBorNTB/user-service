package com.sejong.userservice.infrastructure.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByNickname(String nickname);

    void deleteByNickname(String username);

    UserEntity findByRealName(String realName);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    void deleteByUsername(String username);
}
