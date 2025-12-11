package com.sejong.userservice.domain.role.domain;

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
public class RoleChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String realName;
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole previousRole;

    @Enumerated(EnumType.STRING)
    private UserRole requestedRole;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    private String processedBy;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

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
