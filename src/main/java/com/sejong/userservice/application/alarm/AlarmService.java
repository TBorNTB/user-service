package com.sejong.userservice.application.alarm;

import com.sejong.userservice.application.alarm.dto.AlarmDto;
import com.sejong.userservice.core.alarm.Alarm;
import com.sejong.userservice.core.alarm.AlarmRepository;
import com.sejong.userservice.core.alarm.AlarmType;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    public void save(AlarmDto alarmDto){
        User actorUser = userRepository.findByUsername(alarmDto.getActorUsername());
        Alarm alarm = Alarm.from(alarmDto, actorUser.getNickname());
        alarmRepository.save(alarm);
    }

    public List<Alarm> findAll(String username, AlarmType alarmType) {
        List<Alarm> alarms = alarmRepository.findAllAlarms(username, alarmType);
        return alarms;
    }
}
