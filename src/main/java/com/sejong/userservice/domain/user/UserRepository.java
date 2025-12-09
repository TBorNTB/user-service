package com.sejong.userservice.domain.user;

import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.user.domain.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    User save(User user);

    User findByUsername(String username);

    User findByEmail(String email);

    Page<User> findAllUsers(Pageable pageable);

    void updateUserRole(Long id, UserRole userRole);

    void deleteByUsername(String username);

    List<User> findUsernamesIn(List<String> usernames);

    boolean existsByUsername(String username);

    boolean existsByUsernames(String username, List<String> collaboratorUsernames);

    Long findUsersCount();
}
