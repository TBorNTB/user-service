package com.sejong.userservice.infrastructure.alarm.repository;

import com.sejong.userservice.core.alarm.AlarmType;
import com.sejong.userservice.infrastructure.alarm.AlarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaAlarmRepository extends JpaRepository<AlarmEntity, Long> {

    List<AlarmEntity> findAllByOwnerUsernameAndAlarmTypeAndIsSeenFalse(String ownerUsername, AlarmType alarmType);
}
