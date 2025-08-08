package com.sejong.userservice.core.user;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    User save(User userEntity);

    boolean existsByNickname(String nickname);

    User findByUsername(String username);

    List<User> findAllUsers();

    String deleteByUserNickname(String username);

    List<User> findAllByUsernameIn(List<String> userIds);
}
