package com.sejong.userservice.domain.alarm.repository;

import com.sejong.userservice.domain.alarm.domain.Alarm;
import com.sejong.userservice.domain.alarm.domain.AlarmType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    @Query("SELECT a FROM Alarm a WHERE a.ownerUsername = :ownerUsername AND a.alarmType = :alarmType AND a.seen = false")
    List<Alarm> findUncheckedAlarmsBy(@Param("ownerUsername") String ownerUsername, @Param("alarmType") AlarmType alarmType);

    @Query(value = "SELECT a FROM Alarm a WHERE a.ownerUsername = :ownerUsername " +
            "AND (:alarmType IS NULL OR a.alarmType = :alarmType) " +
            "AND (:seen IS NULL OR a.seen = :seen)",
            countQuery = "SELECT COUNT(a) FROM Alarm a WHERE a.ownerUsername = :ownerUsername " +
                    "AND (:alarmType IS NULL OR a.alarmType = :alarmType) " +
                    "AND (:seen IS NULL OR a.seen = :seen)")
    Page<Alarm> findByOwnerUsernameAndOptionalFilters(
            @Param("ownerUsername") String ownerUsername,
            @Param("alarmType") AlarmType alarmType,
            @Param("seen") Boolean seen,
            Pageable pageable);

    long countByOwnerUsernameAndSeen(String ownerUsername, boolean seen);

    List<Alarm> findByOwnerUsernameAndIdIn(String ownerUsername, List<Long> alarmIds);

    @Modifying
    @Query("UPDATE Alarm a SET a.seen = true WHERE a.ownerUsername = :ownerUsername AND a.seen = false")
    int markAllAsSeenByOwner(@Param("ownerUsername") String ownerUsername);

    @Modifying
    @Query("DELETE FROM Alarm a WHERE a.ownerUsername = :ownerUsername AND a.seen = true")
    int deleteAllReadByOwner(@Param("ownerUsername") String ownerUsername);

    void deleteByOwnerUsernameAndIdIn(String ownerUsername, List<Long> alarmIds);

    @Modifying
    @Query("DELETE FROM Alarm a WHERE a.ownerUsername = :ownerUsername")
    int deleteAllByOwner(@Param("ownerUsername") String ownerUsername);
}
