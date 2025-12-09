package com.sejong.userservice.domain.alarm.service;

import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.domain.Alarm;
import com.sejong.userservice.domain.alarm.domain.AlarmType;
import com.sejong.userservice.domain.alarm.repository.AlarmRepository;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import java.util.List;
import com.sejong.userservice.application.common.exception.BaseException;
import com.sejong.userservice.application.common.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    @Transactional
    public void save(AlarmDto alarmDto){
        User actorUser = userRepository.findByUsername(alarmDto.getActorUsername());
        Alarm alarm = Alarm.from(alarmDto, actorUser.getNickname());
        alarmRepository.save(alarm);
    }

    public List<AlarmDto> findAll(String username, AlarmType alarmType) {
        List<Alarm> alarms = alarmRepository.findUncheckedAlarmsBy(username, alarmType);
        return alarms.stream().map(AlarmDto::from).toList();
    }

    @Transactional
    public void markAsSeen(String username, Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new BaseException(ExceptionType.ALARM_NOT_FOUND));

        if (!alarm.getOwnerUsername().equals(username)) {
            throw new BaseException(ExceptionType.ALARM_ACCESS_DENIED);
        }

        alarm.setSeen(true);
    }
}
