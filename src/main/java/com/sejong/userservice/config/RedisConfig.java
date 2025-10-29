package com.sejong.userservice.config;

import com.sejong.userservice.application.chat.subscriber.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisSubscriber redisSubscriber;

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("chatroom");
    }

    //이거 빈으로 등록해야 백그라운드 컴포넌트가 계속 일을 처리함. 계속 살아있는 “리스너 스레드”를 돌린다고 한다.
    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory cf, RedisSubscriber redisSubscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(cf);
        container.addMessageListener(new MessageListenerAdapter(redisSubscriber),topic());
        return container;
    }
}