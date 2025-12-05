package com.sejong.userservice.alarm.domain;

import lombok.Getter;

@Getter
public enum DomainType {

    NEWS("뉴스"),
    PROJECT("프로젝트"),
    DOCUMENT("다큐먼트"),
    ARTICLE("아티클"),
    GLOBAL(""),
    ;

    private final String name;

    DomainType(String name) {
        this.name = name;
    }
}
