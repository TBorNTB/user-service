package com.sejong.userservice.core.user;

import java.util.List;

public interface UserRepository {
    User save(User user);

    boolean existsByNickname(String nickname);

    User findByUsername(String username);

    List<User> findAllUsers();

    void deleteByUsername(String username);

    List<User> findAllByUsernameIn(List<String> userIds);

    User findByEmail(String email);

    boolean existsByUsernames(String username, List<String> collaboratorUsernames);

    List<User> findByUsernameIn(List<String> usernames);

    boolean existsByUsername(String username);

    void updateUserRole(Long id, String userRole);

    User getUserInfo(String username);
}
