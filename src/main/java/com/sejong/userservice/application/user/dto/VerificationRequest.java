package com.sejong.userservice.application.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {

    @Email
    @NotEmpty
    private String email;

    @Setter
    private String randomCode;

}
