package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.dto.request.UserEmailVerificationReq;
import com.back.baton.domain.user.dto.request.UserEmailVerificationSendReq;
import com.back.baton.domain.user.service.AuthService;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.global.util.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({
        AuthEmailVerificationControllerTest.TestSecurityConfig.class,
        CookieUtil.class
})
class AuthEmailVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Test
    @DisplayName("email-send returns success and delegates to auth service")
    void emailSend_success() throws Exception {
        // given
        UserEmailVerificationSendReq request = new UserEmailVerificationSendReq("user@example.com");

        // when & then
        mockMvc.perform(post("/api/v1/auth/email-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_EMAIL_SEND_SUCCESS.getCode()));

        verify(authService).sendEmailVerificationCode("user@example.com");
    }

    @Test
    @DisplayName("email-verification returns success and delegates to auth service")
    void emailVerification_success() throws Exception {
        // given
        UserEmailVerificationReq request = new UserEmailVerificationReq("user@example.com", "123456");

        // when & then
        mockMvc.perform(post("/api/v1/auth/email-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_EMAIL_VERIFICATION_SUCCESS.getCode()));

        verify(authService).verifyEmail("user@example.com", "123456");
    }

    @Test
    @DisplayName("email-verification rejects invalid code format")
    void emailVerification_fail_whenCodeFormatInvalid() throws Exception {
        // given
        String requestJson = """
                {
                  "email": "user@example.com",
                  "verificationCode": "12345"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/email-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).verifyEmail(anyString(), anyString());
    }
}
