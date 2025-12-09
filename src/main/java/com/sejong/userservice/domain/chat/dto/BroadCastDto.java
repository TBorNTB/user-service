package com.sejong.userservice.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BroadCastDto {
    private ChatMessageDto chatMessageDto;

    public static BroadCastDto of (ChatMessageDto chatMessageDto) {
        return BroadCastDto.builder()
                .chatMessageDto(chatMessageDto)
                .build();
    }
}
