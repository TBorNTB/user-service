package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface SpringDataJpaUserRepository extends JpaRepository<UserEntity,Long> {
}
