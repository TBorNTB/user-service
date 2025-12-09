package com.sejong.userservice.domain.rolechange.domain;

import com.sejong.userservice.core.user.User;
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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user-log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoleChangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String realName;
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserRole requestedRole;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    private String processedBy;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    public static RoleChangeEntity from(User user, UserRole requestedRole, RequestStatus requestStatus) {
        return RoleChangeEntity.builder()
                .id(null)
                .realName(user.getRealName())
                .role(user.getRole())
                .requestedRole(requestedRole)
                .email(user.getEmail())
                .requestedAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .requestStatus(requestStatus)
                .processedBy(null)
                .build();
    }

    public void updateAccept(String adminUsername) {
       this.processedBy = adminUsername;
       this.processedAt = LocalDateTime.now();
       this.requestStatus = RequestStatus.APPROVED;
    }

    public void updateReject(String adminUsername) {
        this.processedBy = adminUsername;
        this.processedAt = LocalDateTime.now();
        this.requestStatus = RequestStatus.REJECTED;
    }
}
