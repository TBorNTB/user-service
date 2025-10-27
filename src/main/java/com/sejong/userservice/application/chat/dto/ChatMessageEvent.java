package com.sejong.userservice.application.chat.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.core.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageEvent {
    private String type;
    private String roomId;
    private String username;
    private String nickname;
    private String content;
    private String imageUrl;

    public static ChatMessageEvent from(ChatMessageDto chatMessageDto) {
        return ChatMessageEvent.builder()
                .type(chatMessageDto.getType())
                .roomId(chatMessageDto.getRoomId())
                .username(chatMessageDto.getUsername())
                .nickname(chatMessageDto.getNickname())
                .content(chatMessageDto.getContent())
                .imageUrl(chatMessageDto.getImageUrl())
                .build();
    }

    public static ChatMessageEvent from(String message){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(message,ChatMessageEvent.class);
        }catch(JsonProcessingException e){
            throw new RuntimeException("JSON 파싱 실패");
        }
    }
}
