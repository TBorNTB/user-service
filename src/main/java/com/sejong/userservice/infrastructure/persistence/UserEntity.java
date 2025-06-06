package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @Column(nullable = false,length = 50)
    private String encryptPassword;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserEntity from(User user){
        return UserEntity.builder()
                .id(user.getId())
                .username(user.getName())
                .encryptPassword(user.getEncryptPassword())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toUser(){
        return User.builder()
                .id(this.id)
                .name(this.username)
                .encryptPassword(this.encryptPassword)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
