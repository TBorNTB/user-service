package com.sejong.userservice.infrastructure.rolechange;

import com.sejong.userservice.core.user.RoleChange;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private String requestedRole;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    private String processedBy;


   public static RoleChangeEntity from(RoleChange roleChange) {
       return RoleChangeEntity.builder()
               .realName(roleChange.getRealName())
               .role(roleChange.getRole())
               .email(roleChange.getEmail())
               .requestedRole(roleChange.getRequestedRole())
               .requestedAt(roleChange.getRequestedAt())
               .processedAt(roleChange.getProcessedAt())
               .requestStatus(roleChange.getRequestStatus())
               .processedBy(roleChange.getProcessedBy())
               .build();
   }

   public RoleChange toDomain(){
       return RoleChange.builder()
               .id(id)
               .realName(realName)
               .role(role)
               .email(email)
               .requestedRole(requestedRole)
               .requestedAt(requestedAt)
               .processedAt(processedAt)
               .requestStatus(requestStatus)
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
