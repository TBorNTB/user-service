package com.sejong.userservice.client.email;

public interface EmailSender {
    String send(String to, String code);
}
