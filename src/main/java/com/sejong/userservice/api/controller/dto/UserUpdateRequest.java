package com.sejong.userservice.api.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

 import jakarta.validation.constraints.Email;
 import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    @NotBlank(message = "Real name cannot be empty")
    private String realName;

    @Email(message = "Invalid email format")
    private String email;

    private Integer grade;

    private String major;

}