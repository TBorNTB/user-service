package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List; // Import List
import java.util.stream.Collectors; // Import Collectors

@Repository
@RequiredArgsConstructor
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataJpaRefreshTokenRepository springDataJpaRefreshTokenRepository;

    @Override
    @Transactional
    public void saveRefreshToken(String token, String username, LocalDateTime expiryDate, String jti) {
        // 기존에 해당 JTI를 가진 토큰이 있을 경우 업데이트하거나 새로 저장 (RTR 구현 방식에 따라 다름)
        // 여기서는 단순히 새로 저장.
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .token(token)
                .username(username)
                .expiryDate(expiryDate)
                .jti(jti)
                .revoked(false) // 최초 생성 시 무효화되지 않음
                .build();
        springDataJpaRefreshTokenRepository.save(refreshTokenEntity);
    }

    @Override
    public Optional<String> findUsernameByToken(String token) {
        return springDataJpaRefreshTokenRepository.findByToken(token)
                .map(RefreshTokenEntity::getUsername); // 사용자 이름만 반환
    }

    @Override
    public Optional<String> findUsernameByJti(String jti) {
        return springDataJpaRefreshTokenRepository.findByJti(jti)
                .map(RefreshTokenEntity::getUsername);
    }

    @Override
    @Transactional
    public boolean revokeToken(String token) {
        return springDataJpaRefreshTokenRepository.findByToken(token)
                .map(refreshTokenEntity -> {
                    refreshTokenEntity.setRevoked(true); // 무효화 상태로 변경
                    springDataJpaRefreshTokenRepository.save(refreshTokenEntity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean revokeTokenByJti(String jti) {
        return springDataJpaRefreshTokenRepository.findByJti(jti)
                .map(refreshTokenEntity -> {
                    refreshTokenEntity.setRevoked(true);
                    springDataJpaRefreshTokenRepository.save(refreshTokenEntity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean isTokenValidOnServer(String token) {
        return springDataJpaRefreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked() && rt.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public void revokeAllTokensForUser(String username) {
        List<RefreshTokenEntity> userTokens = springDataJpaRefreshTokenRepository.findAllByUsername(username);
        userTokens.forEach(token -> token.setRevoked(true)); // 모든 토큰 무효화
        springDataJpaRefreshTokenRepository.saveAll(userTokens);
        // 또는 deleteAllByUsername(username);으로 완전히 삭제할 수도 있음 (정책에 따라)
    }
}