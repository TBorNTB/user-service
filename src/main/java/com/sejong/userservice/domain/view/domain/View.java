package com.sejong.userservice.domain.view.domain;

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
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        name = "view",
        uniqueConstraints = {@UniqueConstraint(name = "uk_post", columnNames = {"postType","postId"})}
)
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class View {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    private Long postId;

    private Long viewCount;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static View of(PostType postType, Long postId, Long viewCount) {
        LocalDateTime now = LocalDateTime.now();
        return View.builder()
                .postType(postType)
                .postId(postId)
                .viewCount(viewCount)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateViewCount(Long viewCount) {
        this.viewCount = viewCount;
        this.updatedAt = LocalDateTime.now();
    }
}
