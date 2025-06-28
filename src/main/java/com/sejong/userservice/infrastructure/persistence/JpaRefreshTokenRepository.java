package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataJpaRefreshTokenRepository springDataJpaRefreshTokenRepository;

    @Override
    @Transactional
    public void saveRefreshToken(String token, String username, LocalDateTime expiryDate, String jti) {

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.issue(token, username, expiryDate, jti);
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