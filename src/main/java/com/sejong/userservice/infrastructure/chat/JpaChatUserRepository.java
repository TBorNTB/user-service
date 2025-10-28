package com.sejong.userservice.infrastructure.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChatUserRepository extends JpaRepository<ChatUserEntity, Long> {
}
