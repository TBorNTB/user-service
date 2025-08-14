package com.sejong.userservice.application.jwt.dto;

import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GithubResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    public String getProviderId() {
        // github 사용자명
        return attribute.get("id").toString();
    }

    @Override
    public String getNickname() {
        return attribute.get("login").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getAvatarUrl() {
        return attribute.get("avatar_url").toString();
    }
}
