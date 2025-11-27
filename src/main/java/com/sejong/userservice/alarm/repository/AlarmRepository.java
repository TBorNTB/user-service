package com.sejong.userservice.alarm.repository;

import com.sejong.userservice.alarm.domain.Alarm;
import com.sejong.userservice.alarm.domain.AlarmType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    @Query("SELECT a FROM Alarm a WHERE a.ownerUsername = :ownerUsername AND a.alarmType = :alarmType AND a.seen = false")
    List<Alarm> findUncheckedAlarmsBy(@Param("ownerUsername")String ownerUsername, @Param("alarmType")AlarmType alarmType);
}
