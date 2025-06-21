package com.sejong.userservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List; // List import

@Repository
public interface SpringDataJpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
    Optional<RefreshTokenEntity> findByJti(String jti); // JTI로 조회
    List<RefreshTokenEntity> findAllByUsername(String username); // 특정 사용자의 모든 토큰 조회
    void deleteByToken(String token); // 특정 토큰 삭제
    void deleteAllByUsername(String username); // 특정 사용자의 모든 토큰 삭제 (로그아웃 시 유용)
}