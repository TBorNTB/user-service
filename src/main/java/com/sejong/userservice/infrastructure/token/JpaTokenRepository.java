package com.sejong.userservice.infrastructure.token;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTokenRepository extends JpaRepository<TokenEntity, Long> {
    Optional<TokenEntity> findByToken(String token);

    Optional<TokenEntity> findByJti(String jti);

    List<TokenEntity> findAllByUsername(String username);

    void deleteByToken(String token);

    void deleteAllByUsername(String username);
}