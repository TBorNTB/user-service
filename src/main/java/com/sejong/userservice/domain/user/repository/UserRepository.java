package com.sejong.userservice.domain.user.repository;

import com.sejong.userservice.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUsernameIn(List<String> usernames);

    @Query("select count(u) from User u")
    Long findUserCount();
}
