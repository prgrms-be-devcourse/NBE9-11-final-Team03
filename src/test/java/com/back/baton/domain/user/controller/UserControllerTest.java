package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.entity.UserRole;
import com.back.baton.domain.user.service.AuthService;
import com.back.baton.domain.user.service.UserService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.filter.JwtAuthenticationFilter;
import com.back.baton.global.response.code.UserErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Date;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({
        JwtTokenProvider.class,
        UserControllerTest.TestSecurityConfig.class,
        CookieUtil.class
})
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean // 또는 @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WebApplicationContext context;
    @MockitoBean private UserService userService;
    @MockitoBean private CookieUtil cookieUtils;

    private String VALID_ACCESS_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.validToken...";
    private final String INVALID_ACCESS_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalidTokenabc";
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable()) // 테스트 환경 CSRF 해제
                    .authorizeHttpRequests(auth -> auth
                            // 🌟 핵심 보정: 로그아웃 API 요청은 무조건 인증(authenticated)된 유저만 허용합니다.
//                            .requestMatchers("/api/v1/auth/logout").authenticated()
                            .requestMatchers("/api/v1/users/me").authenticated()
                            .anyRequest().permitAll()
                    );
            return http.build();
        }
    }

    @BeforeEach
    void setup() {
        // 💡 매 테스트 시작 전 bearerToken을 완전히 초기화하여 "Bearer Bearer ..." 문자열 중복 누적을 방지합니다.
        VALID_ACCESS_TOKEN = "Bearer " + jwtTokenProvider.createAccessToken(1L, UserRole.USER.toString(), new Date());

        // 💡 실제 문지기 필터(JwtAuthenticationFilter)를 억지로 끼워 넣어서 MockMvc를 조립합니다.
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilters(new JwtAuthenticationFilter(jwtTokenProvider))
                .build();
    }

    @Test
    @DisplayName("탈퇴 성공 - 모든 조건 만족 시 200 반환 및 쿠키 삭제")
    @WithMockUser(username = "1")
    void withdraw_success() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", VALID_ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).withdraw(1L);
        verify(cookieUtils).setCookie(any(HttpServletResponse.class), eq("refreshToken"), isNull(), eq(0L));
    }

    @Test
    @DisplayName("탈퇴 실패 - 진행중인 에스크로 거래 존재 시 400 반환")
    @WithMockUser(username = "1")
    void withdraw_fail_escrow_exists() throws Exception {
        // 서비스에서 에러 발생 시뮬레이션
        doThrow(new CustomException(UserErrorCode.ESCROW_IN_PROGRESS))
                .when(userService).withdraw(1L);

        mockMvc.perform(delete("/api/v1/users/me")
                .header("Authorization", VALID_ACCESS_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(UserErrorCode.ESCROW_IN_PROGRESS.getCode()));

        // 에러 시 쿠키 삭제는 호출되지 않아야 함
        verify(cookieUtils, never()).setCookie(any(), any(), any(), any());
    }

    @Test
    @DisplayName("탈퇴 실패 - 인증되지 않은 사용자가 요청 시 401 반환")
    void withdraw_fail_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isForbidden()); // 혹은 403 Forbidden

        verify(userService, never()).withdraw(anyLong());
    }
    @Test
    @DisplayName("탈퇴 실패 - 존재하지 않는 유저가 요청 시 404 반환")
    @WithMockUser(username = "999") // 존재하지 않는 유저 ID로 가정
    void withdraw_fail_user_not_found() throws Exception {
        // given: 유저를 찾을 수 없을 때 예외 발생
        doThrow(new CustomException(UserErrorCode.USER_NOT_FOUND))
                .when(userService).withdraw(999L);

        // when & then
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", VALID_ACCESS_TOKEN))
                .andExpect(status().isNotFound()) // 404 응답
                .andExpect(jsonPath("$.code").value(UserErrorCode.USER_NOT_FOUND.getCode()));

        verify(cookieUtils, never()).setCookie(any(), any(), any(), any());
    }

    @Test
    @DisplayName("탈퇴 실패 - 서버 오류 시 500 반환")
    @WithMockUser(username = "1")
    void withdraw_fail_internal_server_error() throws Exception {
        // given: 예기치 못한 런타임 에러 발생
        doThrow(new RuntimeException("Database connection error"))
                .when(userService).withdraw(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", VALID_ACCESS_TOKEN))
                .andExpect(status().isInternalServerError());

        verify(cookieUtils, never()).setCookie(any(), any(), any(), any());
    }

    @Test
    @DisplayName("탈퇴 실패 - 삭제된 유저 혹은 이미 탈퇴한 유저 재요청 시")
    @WithMockUser(username = "1")
    void withdraw_fail_already_withdrawn() throws Exception {
        // given
        doThrow(new CustomException(UserErrorCode.USER_NOT_FOUND))
                .when(userService).withdraw(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", VALID_ACCESS_TOKEN))
                .andExpect(status().isNotFound());
    }
}
