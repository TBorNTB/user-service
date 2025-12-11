package com.sejong.userservice.support.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class ServerIdProvider {

    @Value("${eureka.instance.instance-id}")
    private String serverId;

    @PostConstruct
    public void init() {
        log.info("서버 ID 초기화 (Eureka Instance ID): {}", serverId);
    }
}