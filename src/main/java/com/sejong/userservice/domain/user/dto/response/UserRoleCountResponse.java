package com.sejong.userservice.domain.user.dto.response;

import lombok.Builder;

@Builder
public record UserRoleCountResponse(
        Long guestCount,           // 외부인
        Long associateMemberCount, // 준회원
        Long fullMemberCount,      // 정회원
        Long seniorCount,          // 선배님
        Long adminCount,           // 운영진
        Long totalCount            // 전체 회원
) {
}

