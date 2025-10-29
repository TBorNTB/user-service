package com.sejong.userservice.core.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomProvider {
    private static SecureRandom random = new SecureRandom();

    public static String generateRandomCode(int length) {
        int upperLimit = (int) Math.pow(10, length);
        return String.valueOf(random.nextInt(upperLimit));
    }
}

