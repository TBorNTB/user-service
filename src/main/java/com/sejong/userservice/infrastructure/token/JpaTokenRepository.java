package com.sejong.userservice.infrastructure.token;

import com.sejong.userservice.core.token.TokenRepository;
import com.sejong.userservice.core.token.TokenType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JpaTokenRepository implements TokenRepository {

    private final SpringDataJpaTokenRepository springDataJpaTokenRepository;

    @Override
    @Transactional
    public String saveToken(String token, String username, LocalDateTime expiryDate, String jti, TokenType tokenType) {
        TokenEntity tokenEntity = TokenEntity.issue(token, username, expiryDate, jti, tokenType);
        springDataJpaTokenRepository.save(tokenEntity);

        return username;
    }

    @Override
    public String findUsernameByToken(String token) {
        return springDataJpaTokenRepository.findByToken(token)
                .map(TokenEntity::getUsername)
                .orElseThrow(() -> new RuntimeException("사용자 이름을 찾을 수 없어요. 토큰의 유효성을 검사해주세요."));
    }

    @Override
    public String findUsernameByJti(String jti) {
        return springDataJpaTokenRepository.findByJti(jti)
                .map(TokenEntity::getUsername).orElseThrow(() -> new RuntimeException("존재하지 않는 JTI에요."));
    }

    @Override
    @Transactional
    public boolean revokeToken(String token) {
        return springDataJpaTokenRepository.findByToken(token)
                .map(tokenEntity -> {
                    tokenEntity.setRevoked(true);
                    springDataJpaTokenRepository.save(tokenEntity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean revokeTokenByJti(String jti) {
        return springDataJpaTokenRepository.findByJti(jti)
                .map(tokenEntity -> {
                    tokenEntity.setRevoked(true);
                    springDataJpaTokenRepository.save(tokenEntity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean isTokenValidOnServer(String token) {
        return springDataJpaTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked() && rt.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public void revokeAllTokensForUser(String username) {
        List<TokenEntity> userTokens = springDataJpaTokenRepository.findAllByUsername(username);
        userTokens.forEach(token -> token.setRevoked(true));
        springDataJpaTokenRepository.saveAll(userTokens);
    }
}