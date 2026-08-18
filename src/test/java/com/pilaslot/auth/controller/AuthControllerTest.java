package com.pilaslot.auth.controller;

import com.pilaslot.auth.dto.response.LoginResponse;
import com.pilaslot.auth.service.LoginService;
import com.pilaslot.global.config.TimeConfig;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.global.exception.GlobalExceptionHandler;
import com.pilaslot.global.security.CustomAuthenticationEntryPoint;
import com.pilaslot.global.security.JwtAuthenticationFilter;
import com.pilaslot.global.security.JwtTokenProvider;
import com.pilaslot.global.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        TimeConfig.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void returnsAccessTokenAndMemberForValidCredentials() throws Exception {
        given(loginService.login(any())).willReturn(new LoginResponse(
                "access-token",
                "Bearer",
                new LoginResponse.MemberResponse(1L, "1234", "홍길동")
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberNumber": "1234",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.member.id").value(1))
                .andExpect(jsonPath("$.member.memberNumber").value("1234"))
                .andExpect(jsonPath("$.member.name").value("홍길동"));
    }

    @Test
    void returnsSameUnauthorizedErrorForInvalidCredentials() throws Exception {
        given(loginService.login(any()))
                .willThrow(new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberNumber": "1234",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_LOGIN_CREDENTIALS"))
                .andExpect(jsonPath("$.message")
                        .value("회원번호 또는 비밀번호가 올바르지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void returnsInvalidRequestWhenLoginFieldsAreBlank() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberNumber": " ",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"))
                .andExpect(jsonPath("$.errors.length()").value(2));
    }
}
