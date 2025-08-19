package com.sejong.userservice.core.user;

import java.util.List;

public interface UserRepository {
    User save(User userEntity);

    boolean existsByNickname(String nickname);

    User findByUsername(String username);

    List<User> findAllUsers();

    void deleteByUsername(String username);

    List<User> findAllByUsernameIn(List<String> userIds);

    User findByEmail(String email);
}
