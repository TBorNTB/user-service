package com.sejong.userservice.domain.repository;

import com.sejong.userservice.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository {

    List<User> findAll();
}
