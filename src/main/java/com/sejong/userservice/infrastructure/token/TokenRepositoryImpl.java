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
public class TokenRepositoryImpl implements TokenRepository {

    private final JpaTokenRepository jpaTokenRepository;

    @Override
    @Transactional
    public String saveToken(String token, String username, LocalDateTime expiryDate, String jti, TokenType tokenType) {
        TokenEntity tokenEntity = TokenEntity.issue(token, username, expiryDate, jti, tokenType);
        jpaTokenRepository.save(tokenEntity);

        return username;
    }

    @Override
    public String findUsernameByToken(String token) {
        return jpaTokenRepository.findByToken(token)
                .map(TokenEntity::getUsername)
                .orElseThrow(() -> new RuntimeException("사용자 이름을 찾을 수 없어요. 토큰의 유효성을 검사해주세요."));
    }

    @Override
    public String findUsernameByJti(String jti) {
        return jpaTokenRepository.findByJti(jti)
                .map(TokenEntity::getUsername).orElseThrow(() -> new RuntimeException("존재하지 않는 JTI에요."));
    }

    @Override
    @Transactional
    public boolean revokeToken(String token) {
        return jpaTokenRepository.findByToken(token)
                .map(tokenEntity -> {
                    tokenEntity.setRevoked(true);
                    jpaTokenRepository.save(tokenEntity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean revokeTokenByJti(String jti) {
        return jpaTokenRepository.findByJti(jti)
                .map(tokenEntity -> {
                    tokenEntity.setRevoked(true);
                    jpaTokenRepository.save(tokenEntity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean isTokenValidOnServer(String token) {
        return jpaTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked() && rt.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public void revokeAllTokensForUser(String username) {
        List<TokenEntity> userTokens = jpaTokenRepository.findAllByUsername(username);
        userTokens.forEach(token -> token.setRevoked(true));
        jpaTokenRepository.saveAll(userTokens);
    }
}