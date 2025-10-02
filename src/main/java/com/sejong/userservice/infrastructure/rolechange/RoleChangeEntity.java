package com.sejong.userservice.infrastructure.user;

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

    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String requestedRole;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    private String processedBy;


   public static RoleChangeEntity from(User user, String requestedRole, LocalDateTime requestedAt, String processedBy, RequestStatus requestStatus) {
       return RoleChangeEntity.builder()
               .realName(user.getRealName())
               .role(user.getRole())
               .requestedRole(requestedRole)
               .requestedAt(requestedAt)
               .processedAt(LocalDateTime.now())
               .requestStatus(requestStatus)
               .processedBy(processedBy)
               .build();
   }

   public RoleChange toDomain(){
       return RoleChange.builder()
               .realName(realName)
               .role(role)
               .requestedRole(requestedRole)
               .requestedAt(requestedAt)
               .processedAt(processedAt)
               .requestStatus(requestStatus)
               .build();
   }
}
