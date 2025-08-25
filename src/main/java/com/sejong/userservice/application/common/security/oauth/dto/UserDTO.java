package com.sejong.userservice.application.common.security.oauth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private String role;
    private String name;
    private String username;
}

