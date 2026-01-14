package com.sejong.userservice.domain.view.repository;

import com.sejong.userservice.domain.view.domain.View;
import com.sejong.userservice.support.common.constants.PostType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ViewJPARepository extends JpaRepository<View, Long> {
    Optional<View> findByPostTypeAndPostId(PostType postType, Long postId);

    @Query("SELECT COALESCE(SUM(v.viewCount), 0) FROM View v WHERE v.createdAt >= :startDate")
    Long findTotalViewCountSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(v.viewCount), 0) FROM View v WHERE v.createdAt >= :startDate AND v.createdAt <= :endDate")
    Long findByViewAllCount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(v.viewCount), 0) FROM View v WHERE v.postType = :postType AND v.postId IN :postIds")
    Long sumViewCountByPostTypeAndPostIds(@Param("postType") PostType postType, @Param("postIds") List<Long> postIds);
}
