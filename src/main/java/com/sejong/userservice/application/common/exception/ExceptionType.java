package com.sejong.userservice.application.common.exception;

import lombok.AllArgsConstructor;

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
    NOT_FOUND_USER(400,"사용자를 찾을 수 없습니다."),
    USER_NOT_FOUND_BY_REFRESH_TOKEN(401, "제공된 리프레시 토큰에 해당하는 사용자를 찾을 수 없습니다."),

    ACCESS_TOKEN_NOT_EXPIRED(400, "액세스 토큰이 아직 유효합니다"),
    REFRESH_TOKEN_NOT_FOUND(401, "쿠키에 리프레시 토큰이 없습니다."),
    TOKEN_MISMATCH(400, "액세스 토큰과 리프레시 토큰이 다른 사용자를 가리킵니다."),
    EXPIRED_TOKEN(401, "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요."),
    INVALID_OR_REVOKED_TOKEN(401, "유효하지 않거나 무효화된 리프레시 토큰입니다. 다시 로그인해주세요."),
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
