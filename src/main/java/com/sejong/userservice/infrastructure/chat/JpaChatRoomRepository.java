package com.sejong.userservice.infrastructure.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaChatRoomRepository extends JpaRepository<ChatRoomEntity,String> {
    @Query("SELECT cr FROM ChatRoomEntity cr ")
    List<ChatRoomEntity> findAllRooms();
}
