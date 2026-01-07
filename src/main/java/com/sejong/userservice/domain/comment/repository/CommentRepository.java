package com.sejong.userservice.domain.comment.repository;

import com.sejong.userservice.domain.comment.domain.Comment;
import com.sejong.userservice.support.common.constants.PostType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
    SELECT c FROM Comment c
    WHERE c.postId = :postId
      AND c.postType = :postType
      AND c.parent IS NULL
      AND (:cursorId IS NULL OR :cursorId <= 0 OR c.id < :cursorId)
    ORDER BY c.id DESC 
    """)
    List<Comment> findAllCommentsDesc(
            @Param("postId") Long postId,
            @Param("postType") PostType postType,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
    SELECT c FROM Comment c
    WHERE c.postId = :postId
      AND c.postType = :postType
      AND c.parent IS NULL
      AND (:cursorId IS NULL OR :cursorId <= 0 OR c.id > :cursorId)
    ORDER BY c.id ASC 
    """)
    List<Comment> findAllCommentsAsc(
            @Param("postId") Long postId,
            @Param("postType") PostType postType,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
    SELECT c FROM Comment c
    WHERE c.parent.id = :parentId
      AND (:cursorId IS NULL OR :cursorId <= 0 OR c.id < :cursorId)
    ORDER BY c.id DESC
    """)
    List<Comment> findRepliesDesc(
            @Param("parentId") Long parentId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
    SELECT c FROM Comment c
    WHERE c.parent.id = :parentId
      AND (:cursorId IS NULL OR :cursorId <= 0 OR c.id > :cursorId)
    ORDER BY c.id ASC
    """)
    List<Comment> findRepliesAsc(
            @Param("parentId") Long parentId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

}
