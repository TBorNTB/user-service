package com.sejong.userservice.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

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

    // TODO: (선택 사항) UserEntity와의 관계 설정 (예: @ManyToOne)
    // private UserEntity user;
}