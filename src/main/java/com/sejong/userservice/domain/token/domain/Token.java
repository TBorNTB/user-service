package com.sejong.userservice.domain.token.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean revoked; // 토큰이 무효화되었는지 여부 (한 번 사용되거나 로그아웃 시 true)

    @Column(nullable = false, unique = true, length = 255)
    private String jti; // JWT ID (고유 식별자, 재사용 공격 방지에 중요)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType; // (ACCESS, REFRESH)

    public static Token issue(String token, String username, LocalDateTime expiryDate, String jti,
                              TokenType tokenType) {
        return Token.builder()
                .token(token)
                .username(username)
                .expiryDate(expiryDate)
                .jti(jti)
                .tokenType(tokenType)
                .revoked(false)
                .build();
    }
}