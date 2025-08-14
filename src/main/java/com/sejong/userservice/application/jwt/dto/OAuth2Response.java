package com.sejong.userservice.application.jwt.dto;

public interface OAuth2Response {
    String getProvider();
    String getProviderId();
    String getNickname();
    String getEmail();
    String getName();
    String getAvatarUrl();
}
