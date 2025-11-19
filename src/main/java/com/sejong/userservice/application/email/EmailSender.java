package com.sejong.userservice.application.email;

public interface EmailSender {
    String send(String to, String code);
}
