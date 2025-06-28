package com.sejong.userservice.infrastructure.refreshtoken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500) // JWT 토큰 문자열
    private String token;

    @Column(nullable = false)
    private String username; // 사용자 이름 (사용자 엔티티와 연결)

    @Column(nullable = false)
    private LocalDateTime expiryDate; // 토큰 만료일 (DB에서 관리)

    @Column(nullable = false)
    private boolean revoked; // 토큰이 무효화되었는지 여부 (한 번 사용되거나 로그아웃 시 true)

    @Column(nullable = false, unique = true, length = 255)
    private String jti; // JWT ID (고유 식별자, 재사용 공격 방지에 중요)

    public static RefreshTokenEntity issue(String token, String username, LocalDateTime expiryDate, String jti) {
        return RefreshTokenEntity.builder()
                .token(token)
                .username(username)
                .expiryDate(expiryDate)
                .jti(jti)
                .revoked(false)
                .build();
    }
}