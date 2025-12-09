package com.sejong.userservice.domain.chat.repository;

import com.sejong.userservice.domain.chat.domain.ChatRoomUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    @Modifying
    @Query("DELETE FROM ChatRoomUser cu WHERE cu.user.username = :username AND cu.chatRoom.roomId = :roomId ")
    void deleteByUsernameAndRoomId(@Param("username")String username, @Param("roomId")String roomId);

    @Query("SELECT COUNT(cu) > 0 FROM ChatRoomUser cu WHERE cu.user.username = :username AND cu.chatRoom.roomId = :roomId")
    boolean existsByUsernameAndRoomId(@Param("username")String username, @Param("roomId")String roomId);

    @Query("SELECT count(*) FROM ChatRoomUser cu WHERE  cu.chatRoom.roomId = :roomId")
    Integer getCountByRoomId(@Param("roomId")String roomId);

    @Query("SELECT cu FROM ChatRoomUser cu WHERE cu.user.username= :username")
    List<ChatRoomUser> findAllByUsername(@Param("username")String username);
}
