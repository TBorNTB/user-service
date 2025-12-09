package com.sejong.userservice.core.user;

import com.sejong.userservice.domain.role.domain.UserRole;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    User save(User user);

    boolean existsByNickname(String nickname);

    User findByUsername(String username);

    List<User> findAllUsers();
    Page<User> findAllUsers(Pageable pageable);

    void deleteByUsername(String username);

    List<User> findAllByUsernameIn(List<String> userIds);

    User findByEmail(String email);

    boolean existsByUsernames(String username, List<String> collaboratorUsernames);

    List<User> findByUsernameIn(List<String> usernames);

    boolean existsByUsername(String username);

    void updateUserRole(Long id, UserRole userRole);

    User getUserInfo(String username);

    Long findUsersCount();
}
