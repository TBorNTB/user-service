package com.sejong.userservice.domain.user.repository;

import com.sejong.userservice.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUsernameIn(List<String> usernames);

    @Query("select count(u) from User u")
    Long findUserCount();

    @Query("""
        SELECT u FROM User u
        WHERE (:searchKeyword IS NULL OR :searchKeyword = '' OR 
               LOWER(u.realName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR 
               LOWER(u.email) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))
          AND (:cursorId IS NULL OR :cursorId <= 0 OR u.id < :cursorId)
        ORDER BY u.id DESC
        """)
    List<User> searchUsersDesc(
            @Param("searchKeyword") String searchKeyword,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
        SELECT u FROM User u
        WHERE (:searchKeyword IS NULL OR :searchKeyword = '' OR 
               LOWER(u.realName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR 
               LOWER(u.email) LIKE LOWER(CONCAT('%', :searchKeyword, '%')))
          AND (:cursorId IS NULL OR :cursorId <= 0 OR u.id > :cursorId)
        ORDER BY u.id ASC
        """)
    List<User> searchUsersAsc(
            @Param("searchKeyword") String searchKeyword,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
