package com.sejong.userservice.application.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExceptionType implements ExceptionTypeIfs {

    OK(200,"성공"),
    BAD_REQUEST(400,"잘못된 요청"),
    SERVER_ERROR(500,"서버 에러"),

    NULL_POINT(500,"Null Pointer"),
    MULTI_REQUEST(405,"하루 한번만 요청 가능합니다"),
    BAD_SORT_REQUEST(400,"정렬 방향은 ASC/DESC 만 가능합니다."),
    External_Server_Error(500,"서킷 브레이커가 작동했습니다."),


    EMPTY_USER_REQUEST(400,"사용자 닉네임 목록이 비어 있습니다."),
    DUPLICATED_USER_REQUEST(400,"중복된 사용자 닉네임"),
    NOT_FOUND_USER(400,"없는 사용자 닉네임"),
    ;

    private final Integer httpStatus;
    private final String description;

    @Override
    public Integer httpStatus() {
        return httpStatus;
    }

    @Override
    public String description() {
        return description;
    }
}
