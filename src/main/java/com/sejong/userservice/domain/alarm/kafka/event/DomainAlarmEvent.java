package com.sejong.userservice.domain.alarm.kafka.event;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.sejong.userservice.domain.alarm.domain.AlarmType;
import com.sejong.userservice.domain.alarm.domain.DomainType;
import com.sejong.userservice.core.user.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DomainAlarmEvent {
    private AlarmType alarmType;
    private DomainType domainType;
    private Long domainId;
    private String actorUsername;
    private String ownerUsername;

    private LocalDateTime createdAt;

    public static DomainAlarmEvent from(User user, AlarmType alarmType) {
        return DomainAlarmEvent.builder()
                .domainId(user.getId())
                .alarmType(alarmType)
                .domainType(DomainType.GLOBAL)
                .actorUsername(user.getUsername())
                .ownerUsername("NameIsAdmin") //관리자들의 이름을 가져와서 넣어야 되는데 해당 알람 검색할때는 해당 이름으로 검색하도록
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static DomainAlarmEvent fromJson(String message) {
        try {
            System.out.println("📩 Received JSON: " + message);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);

            // ISO_LOCAL_DATE_TIME 사용 (소수점 자리수 상관없음)
            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            JavaTimeModule timeModule = new JavaTimeModule();
            timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(fmt));
            mapper.registerModule(timeModule);

            return mapper.readValue(message, DomainAlarmEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage());
        }
    }
}