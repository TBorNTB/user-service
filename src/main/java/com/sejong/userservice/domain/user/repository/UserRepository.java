package com.sejong.userservice.domain.user.repository;

import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUsernameIn(List<String> usernames);

    @Query("select count(u) from User u")
    Long findUserCount();

    @Query("SELECT u FROM User u WHERE u.role IN :roles " +
           "AND (:nickname IS NULL OR u.nickname LIKE %:nickname%)" +
           "AND (:realName IS NULL OR u.realName LIKE %:realName%)")
    Page<User> findByRolesAndSearch(
            @Param("roles") List<UserRole> roles,
            @Param("nickname") String nickname,
            @Param("realName") String realName,
            Pageable pageable
    );
}
