package com.sejong.userservice.domain.like.repository;

import com.sejong.userservice.domain.like.domain.Like;
import com.sejong.userservice.support.common.constants.PostType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUsernameAndPostIdAndPostType(String username, Long postId, PostType postType);

    boolean existsByUsernameAndPostIdAndPostType(String username, Long postId, PostType postType);

    long countByPostIdAndPostType(Long postId, PostType postType);

    @Query("SELECT CONCAT('post:', l.postType, ':', l.postId, ':like:count'), COUNT(l) " +
            "FROM Like l GROUP BY l.postType, l.postId")
    Map<String, Long> getLikeCountStatistics();

    @Query("SELECT COUNT(l) FROM Like l WHERE l.postType = :postType AND l.postId IN :postIds")
    Long countByPostTypeAndPostIds(@Param("postType") PostType postType, @Param("postIds") List<Long> postIds);

    Page<Like> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
}
