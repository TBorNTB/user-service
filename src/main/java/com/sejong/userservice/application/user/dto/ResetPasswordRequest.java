package com.sejong.userservice.application.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String randomCode;

    @NotEmpty
    private String newPassword;
}
