package com.sejong.userservice.infrastructure.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaChatUserRepository extends JpaRepository<ChatUserEntity, Long> {
    @Modifying
    @Query("DELETE FROM ChatUserEntity cu WHERE cu.username = :username AND cu.roomId = :roomId ")
    void deleteByUsername(@Param("username")String username, @Param("roomId")String roomId);

    @Query("SELECT count(*) FROM ChatUserEntity cu WHERE  cu.roomId = :roomId")
    Integer getCountUserByUsername(@Param("roomId")String roomId);

    @Query("SELECT cu FROM ChatUserEntity cu WHERE cu.username= :username")
    List<ChatUserEntity> findAllByUsername(@Param("username")String username);
}
