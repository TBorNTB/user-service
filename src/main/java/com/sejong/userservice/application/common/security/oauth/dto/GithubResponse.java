package com.sejong.userservice.application.common.security.oauth.dto;

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
        Object email = attribute.get("email");
        return email != null ? email.toString() : attribute.get("login").toString() + "@github.local";
    }

    @Override
    public String getName() { // 실제 이름
        Object name = attribute.get("name");
        return name != null ? name.toString() : attribute.get("login").toString();
    }

    @Override
    public String getAvatarUrl() {
        return attribute.get("avatar_url").toString();
    }
}
