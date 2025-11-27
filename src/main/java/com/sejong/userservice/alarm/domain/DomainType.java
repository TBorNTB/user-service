package com.sejong.userservice.alarm.domain;

import lombok.Getter;

@Getter
public enum DomainType {
    PROJECT("프로젝트"),
    NEWS("뉴스"),
    ARCHIVE("아카이브"),
    GLOBAL(""),
    ;

    private final String name;

    DomainType(String name) {
        this.name = name;
    }
}
