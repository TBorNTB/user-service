package com.sejong.userservice.infrastructure.alarm.repository;

import com.sejong.userservice.core.alarm.Alarm;
import com.sejong.userservice.core.alarm.AlarmRepository;
import com.sejong.userservice.core.alarm.AlarmType;
import com.sejong.userservice.infrastructure.alarm.AlarmEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AlarmRepositoryImpl implements AlarmRepository {

    private final JpaAlarmRepository jpaAlarmRepository;

    @Override
    public void save(Alarm alarm) {
        AlarmEntity alarmEntity = AlarmEntity.fromAlarm(alarm);
        jpaAlarmRepository.save(alarmEntity);
    }

    @Override
    public List<Alarm> findAllAlarms(String username, AlarmType alarmType) {
        List<AlarmEntity> alarmEntities = jpaAlarmRepository.findAllByOwnerUsernameAndAlarmTypeAndIsSeenFalse(username, alarmType);
        return alarmEntities.stream()
                .map(AlarmEntity::toDomain)
                .toList();
    }
}
