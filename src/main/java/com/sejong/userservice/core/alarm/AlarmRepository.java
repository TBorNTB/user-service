package com.sejong.userservice.core.alarm;

import java.util.List;

public interface AlarmRepository {
    void save(Alarm alarm);

    List<Alarm> findAllAlarms(String username, AlarmType alarmType);
}
