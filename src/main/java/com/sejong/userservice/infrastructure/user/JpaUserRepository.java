package com.sejong.userservice.infrastructure.user;

import java.util.List;
import java.util.Optional;

import com.sejong.userservice.core.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByNickname(String nickname);

    void deleteByNickname(String username);

    UserEntity findByRealName(String realName);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByUsername(String username);

    List<UserEntity> findByUsernameIn(List<String> usernames);

    @Query("select count(ue) from UserEntity ue")
    Long findUserCount();
}
