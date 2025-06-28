package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataJpaRefreshTokenRepository springDataJpaRefreshTokenRepository;

    @Override
    @Transactional
    public String saveRefreshToken(String token, String username, LocalDateTime expiryDate, String jti) {
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.issue(token, username, expiryDate, jti);
        springDataJpaRefreshTokenRepository.save(refreshTokenEntity);

        return username;
    }

    @Override
    public String findUsernameByToken(String token) {
        return springDataJpaRefreshTokenRepository.findByToken(token)
                .map(RefreshTokenEntity::getUsername)
                .orElseThrow(() -> new RuntimeException("사용자 이름을 찾을 수 없어요. 토큰의 유효성을 검사해주세요."));
    }

    @Override
    public String findUsernameByJti(String jti) {
        return springDataJpaRefreshTokenRepository.findByJti(jti)
                .map(RefreshTokenEntity::getUsername).orElseThrow(() -> new RuntimeException("존재하지 않는 JTI에요."));
    }

    @Override
    @Transactional
    public boolean revokeToken(String token) {
        return springDataJpaRefreshTokenRepository.findByToken(token)
                .map(refreshTokenEntity -> {
                    refreshTokenEntity.setRevoked(true);
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
        userTokens.forEach(token -> token.setRevoked(true));
        springDataJpaRefreshTokenRepository.saveAll(userTokens);
    }
}