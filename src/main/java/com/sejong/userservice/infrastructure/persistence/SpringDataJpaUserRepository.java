package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface SpringDataJpaUserRepository extends JpaRepository<UserEntity,Long> {
    boolean existsByUsername(String loginId);
    Optional<UserEntity> findByUsername(String username);
}
