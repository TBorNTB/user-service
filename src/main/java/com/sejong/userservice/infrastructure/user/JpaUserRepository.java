package com.sejong.userservice.infrastructure.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByNickname(String nickname);

    Optional<UserEntity> findByNickname(String username);

    void deleteByNickname(String username);

}
