package com.sejong.userservice.config;

import com.sejong.userservice.chat.handler.ChatWebSocketHandler;
import com.sejong.userservice.chat.interceptor.CustomWebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;
    private final CustomWebSocketInterceptor customWebSocketInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatHandler, CustomWebSocketInterceptor customWebSocketInterceptor) {
        this.chatHandler = chatHandler;
        this.customWebSocketInterceptor = customWebSocketInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
                .addInterceptors(customWebSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
