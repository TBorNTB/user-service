package com.sejong.userservice.domain.alarm.service;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_USER;

import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.domain.Alarm;
import com.sejong.userservice.domain.alarm.domain.AlarmType;
import com.sejong.userservice.domain.alarm.repository.AlarmRepository;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.repository.UserRepository;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.exception.type.ExceptionType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** @param seen null=전체, true=읽은 알람만, false=안 읽은 알람만 */
    public Page<AlarmDto> findPage(String username, AlarmType alarmType, Boolean seen, Pageable pageable) {
        Page<Alarm> page = alarmRepository.findByOwnerUsernameAndOptionalFilters(username, alarmType, seen, pageable);
        return page.map(AlarmDto::from);
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

    /** 지정한 ID 목록 중 본인 알람만 일괄 읽음 처리 */
    @Transactional
    public void markAsSeenBulk(String username, List<Long> alarmIds) {
        List<Alarm> alarms = alarmRepository.findByOwnerUsernameAndIdIn(username, alarmIds);
        alarms.forEach(a -> a.setSeen(true));
    }

    /** 전체 알람 일괄 읽음 처리 */
    @Transactional
    public int markAllAsSeen(String username) {
        return alarmRepository.markAllAsSeenByOwner(username);
    }

    /** 미확인 알람 개수 (뱃지 등) */
    public long getUnreadCount(String username) {
        return alarmRepository.countByOwnerUsernameAndSeen(username, false);
    }

    /** 단건 삭제 (본인 알람만) */
    @Transactional
    public void deleteAlarm(String username, Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new BaseException(ExceptionType.ALARM_NOT_FOUND));
        if (!alarm.getOwnerUsername().equals(username)) {
            throw new BaseException(ExceptionType.ALARM_ACCESS_DENIED);
        }
        alarmRepository.delete(alarm);
    }

    /** 지정한 ID 목록 중 본인 알람만 일괄 삭제 */
    @Transactional
    public void deleteBulk(String username, List<Long> alarmIds) {
        alarmRepository.deleteByOwnerUsernameAndIdIn(username, alarmIds);
    }

    /** 읽은 알람 전체 삭제 */
    @Transactional
    public int deleteAllRead(String username) {
        return alarmRepository.deleteAllReadByOwner(username);
    }

    /** 전체 알람 삭제 (읽음/안읽음 구분 없이 모두) */
    @Transactional
    public int deleteAll(String username) {
        return alarmRepository.deleteAllByOwner(username);
    }
}
