package com.sejong.userservice.common.exception;

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

    EMAIL_TRANSFER_ERROR(501, "메일 전송 중 오류 발생"),
    UNSUPPORTED_EMAIL_ADDRESS(401, "이메일 주소 인코딩 오류 발생"),
    UNEXPECTED_ERROR(500, "예상치 못한 오류 발생"),

    VERIFICATION_CODE_NOT_FOUND(400, "인증 코드가 만료되었거나 존재하지 않습니다."),
    VERIFICATION_CODE_MISMATCH(400, "인증 코드가 일치하지 않습니다."),
    SAME_WITH_PREVIOUS_PASSWORD(400, "새 비밀번호는 이전 비밀번호와 달라야 합니다."),


    ROOM_ID_NOT_FOUND(400, "존재하지 않는 채팅룸 Id 입니다."),
    DM_ROOM_WITH_OTHER_PERSON(400, "자기 자신과의 DM 룸을 생성할 수 없습니다."),

    ALARM_NOT_FOUND(404, "알람을 찾을 수 없습니다."),
    ALARM_ACCESS_DENIED(403, "본인의 알람만 읽음 처리할 수 있습니다."),

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
