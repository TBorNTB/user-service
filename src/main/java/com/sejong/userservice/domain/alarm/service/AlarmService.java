package com.sejong.userservice.domain.alarm.service;

import static com.sejong.userservice.support.common.exception.ExceptionType.NOT_FOUND_USER;

import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.domain.Alarm;
import com.sejong.userservice.domain.alarm.domain.AlarmType;
import com.sejong.userservice.domain.alarm.repository.AlarmRepository;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.repository.UserRepository;
import com.sejong.userservice.support.common.exception.BaseException;
import com.sejong.userservice.support.common.exception.ExceptionType;
import java.util.List;
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
        User actorUser = userRepository.findByUsername(alarmDto.getActorUsername()).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
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
