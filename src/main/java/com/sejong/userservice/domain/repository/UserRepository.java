package com.sejong.userservice.domain.repository;

import com.sejong.userservice.domain.model.User;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    User save(User userEntity);
    boolean existsByUsername(String loginId);
    User findByUsername(String username);
    List<User> findAllUsers();
}
