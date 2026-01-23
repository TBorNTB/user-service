package com.sejong.userservice.domain.like.domain;

import com.sejong.userservice.support.common.constants.PostType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "postlike",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_post_type", columnNames = {"username", "postId", "postType"})
        }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private PostType postType;
    private Long postId;
    private String username;

    private LocalDateTime createdAt;

    public static Like of(String username, Long postId, PostType postType, LocalDateTime createdAt) {
        return Like.builder()
                .id(null)
                .postType(postType)
                .postId(postId)
                .username(username)
                .createdAt(createdAt)
                .build();
    }
}
