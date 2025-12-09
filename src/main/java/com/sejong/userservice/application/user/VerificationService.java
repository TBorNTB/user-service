package com.sejong.userservice.application.user;

import com.sejong.userservice.application.user.dto.ResetPasswordRequest;
import com.sejong.userservice.application.user.dto.VerificationRequest;
import com.sejong.userservice.client.email.EmailSender;
import com.sejong.userservice.core.util.RandomProvider;
import com.sejong.userservice.infrastructure.redis.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final EmailSender emailSender;
    private final CacheService cacheService;

    public void sendVerificationCode(VerificationRequest request) {
        String code = RandomProvider.generateRandomCode(8);
        emailSender.send(request.getEmail(), code);
        cacheService.save(request.getEmail(), code);
    }

    public void verifyEmailCode(ResetPasswordRequest request) {
        cacheService.verify(request.getEmail(), request.getRandomCode());
    }
}
