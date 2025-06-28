package com.sejong.userservice.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);

    Optional<RefreshTokenEntity> findByJti(String jti);

    List<RefreshTokenEntity> findAllByUsername(String username);

    void deleteByToken(String token);

    void deleteAllByUsername(String username);
}