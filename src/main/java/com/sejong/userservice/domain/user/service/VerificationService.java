package com.sejong.userservice.domain.user.service;

import com.sejong.userservice.client.email.EmailSender;
import com.sejong.userservice.domain.user.dto.request.ResetPasswordRequest;
import com.sejong.userservice.domain.user.dto.request.VerificationRequest;
import com.sejong.userservice.domain.user.repository.VerificationCacheService;
import com.sejong.userservice.support.common.RandomProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final EmailSender emailSender;
    private final VerificationCacheService verificationCacheService;

    public void sendVerificationCode(VerificationRequest request) {
        String code = RandomProvider.generateRandomCode(8);
        emailSender.send(request.getEmail(), code);
        verificationCacheService.save(request.getEmail(), code);
    }

    public void verifyEmailCode(ResetPasswordRequest request) {
        verificationCacheService.verify(request.getEmail(), request.getRandomCode());
    }
}
