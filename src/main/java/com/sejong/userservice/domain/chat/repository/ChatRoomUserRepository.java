package com.sejong.userservice.domain.chat.repository;

import com.sejong.userservice.domain.chat.domain.ChatRoomUser;
import com.sejong.userservice.domain.chat.repository.projection.ChatRoomSummaryProjection;
import com.sejong.userservice.domain.chat.repository.projection.RoomCountProjection;
import com.sejong.userservice.domain.chat.repository.projection.RoomUnreadCountProjection;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    @Modifying
    @Query("DELETE FROM ChatRoomUser cu WHERE cu.user.id = :userId AND cu.chatRoom.roomId = :roomId ")
    void deleteByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") String roomId);

    @Query("SELECT COUNT(cu) > 0 FROM ChatRoomUser cu WHERE cu.user.id = :userId AND cu.chatRoom.roomId = :roomId")
    boolean existsByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") String roomId);

    @Modifying
    @Query("DELETE FROM ChatRoomUser cu WHERE cu.user.username = :username AND cu.chatRoom.roomId = :roomId ")
    void deleteByUsernameAndRoomId(@Param("username")String username, @Param("roomId")String roomId);

    @Query("SELECT COUNT(cu) > 0 FROM ChatRoomUser cu WHERE cu.user.username = :username AND cu.chatRoom.roomId = :roomId")
    boolean existsByUsernameAndRoomId(@Param("username")String username, @Param("roomId")String roomId);

    @Query("SELECT count(*) FROM ChatRoomUser cu WHERE  cu.chatRoom.roomId = :roomId")
    Integer getCountByRoomId(@Param("roomId")String roomId);

    @Query("SELECT cu FROM ChatRoomUser cu WHERE cu.user.username= :username")
    List<ChatRoomUser> findAllByUsername(@Param("username")String username);

    @Query("SELECT cu FROM ChatRoomUser cu WHERE cu.user.id = :userId")
    List<ChatRoomUser> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT cu FROM ChatRoomUser cu WHERE cu.user.id = :userId AND cu.chatRoom.roomId = :roomId")
    Optional<ChatRoomUser> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") String roomId);

        @Query("""
            SELECT
            cu.chatRoom.roomId AS roomId,
            cu.chatRoom.roomName AS roomName,
            cu.displayName AS displayName,
                                cm.createdAt AS lastMessageAt,
                    COALESCE(cm.createdAt, cu.chatRoom.createdAt) AS activityAt,
                                cm.content AS lastMessage,
                                cm.imageUrl AS lastMessageImageUrl,
                                cm.username AS lastSenderUsername
            FROM ChatRoomUser cu
                        LEFT JOIN ChatMessage cm
                            ON cm.id = (
                                SELECT MAX(cm2.id)
                                FROM ChatMessage cm2
                                WHERE cm2.chatRoom = cu.chatRoom
                                    AND cm2.type = 'CHAT'
                                    AND (cm2.content IS NOT NULL OR cm2.imageUrl IS NOT NULL)
                            )
            WHERE cu.user.id = :userId
                        ORDER BY COALESCE(cm.createdAt, cu.chatRoom.createdAt) DESC
            """)
        List<ChatRoomSummaryProjection> findRoomSummariesByUserId(@Param("userId") Long userId);

            @Query("""
                SELECT
                cu.chatRoom.roomId AS roomId,
                cu.chatRoom.roomName AS roomName,
                cu.displayName AS displayName,
                cm.createdAt AS lastMessageAt,
                COALESCE(cm.createdAt, cu.chatRoom.createdAt) AS activityAt,
                cm.content AS lastMessage,
                cm.imageUrl AS lastMessageImageUrl,
                cm.username AS lastSenderUsername
                FROM ChatRoomUser cu
                LEFT JOIN ChatMessage cm
                  ON cm.id = (
                SELECT MAX(cm2.id)
                FROM ChatMessage cm2
                WHERE cm2.chatRoom = cu.chatRoom
                  AND cm2.type = 'CHAT'
                  AND (cm2.content IS NOT NULL OR cm2.imageUrl IS NOT NULL)
                  )
                WHERE cu.user.id = :userId
                  AND (
                :cursorAt IS NULL
                OR COALESCE(cm.createdAt, cu.chatRoom.createdAt) < :cursorAt
                OR (COALESCE(cm.createdAt, cu.chatRoom.createdAt) = :cursorAt AND cu.chatRoom.roomId < :cursorRoomId)
                  )
                ORDER BY COALESCE(cm.createdAt, cu.chatRoom.createdAt) DESC, cu.chatRoom.roomId DESC
                """)
            List<ChatRoomSummaryProjection> findRoomSummariesPageByUserId(
                @Param("userId") Long userId,
                @Param("cursorAt") java.time.LocalDateTime cursorAt,
                @Param("cursorRoomId") String cursorRoomId,
                Pageable pageable
            );

            @Query("""
                SELECT cu.chatRoom.roomId AS roomId, COUNT(cu) AS count
                FROM ChatRoomUser cu
                WHERE cu.chatRoom.roomId IN :roomIds
                GROUP BY cu.chatRoom.roomId
                """)
            List<RoomCountProjection> countMembersByRoomIds(@Param("roomIds") List<String> roomIds);

            @Query("""
                SELECT cu.chatRoom.roomId AS roomId, COUNT(cm) AS unreadCount
                FROM ChatRoomUser cu
                LEFT JOIN ChatMessage cm
                  ON cm.chatRoom = cu.chatRoom
                 AND cm.type = 'CHAT'
                 AND (cm.content IS NOT NULL OR cm.imageUrl IS NOT NULL)
                 AND cm.username <> cu.user.username
                 AND (cu.lastReadMessageId IS NULL OR cm.id > cu.lastReadMessageId)
                WHERE cu.user.id = :userId
                  AND cu.chatRoom.roomId IN :roomIds
                GROUP BY cu.chatRoom.roomId
                """)
            List<RoomUnreadCountProjection> countUnreadByRoomIds(
                @Param("userId") Long userId,
                @Param("roomIds") List<String> roomIds
            );

        @Query("""
            SELECT cu
            FROM ChatRoomUser cu
            JOIN FETCH cu.user u
            WHERE cu.chatRoom.roomId IN :roomIds
            """)
        List<ChatRoomUser> findAllByRoomIdsWithUser(@Param("roomIds") List<String> roomIds);

        @Query("""
            SELECT cu1.chatRoom.roomId
            FROM ChatRoomUser cu1, ChatRoomUser cu2
            WHERE cu1.chatRoom = cu2.chatRoom
              AND cu1.user.id = :userId1
              AND cu2.user.id = :userId2
              AND cu1.chatRoom.roomName IS NULL
            """)
        List<String> findExistingDmRoomIdsByUserIds(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
        );

    @Query("SELECT cu FROM ChatRoomUser cu WHERE cu.chatRoom.roomId = :roomId AND cu.user.id <> :userId")
    List<ChatRoomUser> findOthersInRoomByUserId(@Param("roomId") String roomId, @Param("userId") Long userId);

    @Query("SELECT cu FROM ChatRoomUser cu WHERE cu.chatRoom.roomId = :roomId AND cu.user.username <> :username")
    List<ChatRoomUser> findOthersInRoom(@Param("roomId") String roomId, @Param("username") String username);
}
