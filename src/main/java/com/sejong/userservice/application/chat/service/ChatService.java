package com.sejong.userservice.application.chat.service;

import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.dto.ChatMessageEvent;
import com.sejong.userservice.core.chat.ChatMessage;
import com.sejong.userservice.core.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    @Transactional
    public void save(ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = ChatMessage.from(chatMessageDto);
        chatRepository.save(chatMessage);
    }
}
