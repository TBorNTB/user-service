package com.sejong.userservice.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.domain.user.dto.request.JoinRequest;
import com.sejong.userservice.domain.user.service.UserService;
import com.sejong.userservice.domain.user.service.VerificationService;
import com.sejong.userservice.support.common.exception.GlobalExceptionHandler;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.exception.type.ExceptionType;
import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private VerificationService verificationService;

    @Test
        void joinProcess_duplicateEmail_returns409WithMessage() throws Exception {
        when(userService.joinProcess(any())).thenThrow(new BaseException(ExceptionType.DUPLICATE_EMAIL));

        JoinRequest request = new JoinRequest(
                "nick",
                "password123",
                "Real Name",
                "dup@example.com",
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("이미 사용중인 이메일입니다."));
    }

    @Test
        void joinProcess_duplicateNickname_returns409WithMessage() throws Exception {
        when(userService.joinProcess(any())).thenThrow(new BaseException(ExceptionType.DUPLICATE_NICKNAME));

        JoinRequest request = new JoinRequest(
                "dupNick",
                "password123",
                "Real Name",
                "new@example.com",
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("이미 사용중인 닉네임입니다."));
    }
}
