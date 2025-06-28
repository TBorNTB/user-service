package com.sejong.userservice.infrastructure.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaUserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsername(String loginId);

    Optional<UserEntity> findByUsername(String username);

    void deleteByUsername(String username);
}
