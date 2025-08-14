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
    public String getProviderId() { // 깃헙에서 사용하는 유저 pk
        return attribute.get("id").toString();
    }

    @Override
    public String getNickname() { // 깃헙에서 사용하는 닉네임
        return attribute.get("login").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() { // 실제 이름
        return attribute.get("name").toString();
    }

    @Override
    public String getAvatarUrl() {
        return attribute.get("avatar_url").toString();
    }
}
