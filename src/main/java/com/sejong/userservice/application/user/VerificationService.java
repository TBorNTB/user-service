package com.sejong.userservice.application.user;

import com.sejong.userservice.application.email.EmailSender;
import com.sejong.userservice.application.user.dto.ResetPasswordRequest;
import com.sejong.userservice.application.user.dto.VerificationRequest;
import com.sejong.userservice.infrastructure.redis.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final EmailSender emailSender;
    private final CacheService cacheService;

    public void sendVerificationCode(VerificationRequest request) {
        emailSender.send(request.getEmail(), request.getRandomCode());
        cacheService.save(request.getEmail(), request.getRandomCode());
    }

    public void verifyEmailCode(ResetPasswordRequest request) {
        cacheService.verify(request.getEmail(), request.getRandomCode());
    }
}
