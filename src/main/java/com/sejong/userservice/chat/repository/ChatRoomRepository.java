package com.sejong.userservice.chat.repository;

import com.sejong.userservice.chat.domain.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,String> {
    @Query("SELECT cr FROM ChatRoom cr ")
    List<ChatRoom> findAllRooms();
}
