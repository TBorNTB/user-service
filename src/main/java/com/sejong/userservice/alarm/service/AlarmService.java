package com.sejong.userservice.alarm.service;

import com.sejong.userservice.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.alarm.domain.Alarm;
import com.sejong.userservice.alarm.domain.AlarmType;
import com.sejong.userservice.alarm.repository.AlarmRepository;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<AlarmDto> findAll(String username, AlarmType alarmType) {
        List<Alarm> alarms = alarmRepository.findUncheckedAlarmsBy(username, alarmType);
        return alarms.stream().map(AlarmDto::from).toList();
    }
}
