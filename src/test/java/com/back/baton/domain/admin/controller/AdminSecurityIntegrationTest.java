package com.back.baton.domain.admin.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.back.baton.global.security.JwtTokenProvider;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=admin-security-integration-test-secret-key",
        "hash.salt=admin-security-test-salt"
})
@AutoConfigureMockMvc
class AdminSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("일반 유저가 관리자 API를 호출하면 403을 반환한다")
    void userCannotAccessAdminApi() throws Exception {
        String token = jwtTokenProvider.createAccessToken(999L, "USER", new Date());

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH-403-001"));
    }

    @Test
    @DisplayName("비인증 요청이 관리자 API를 호출하면 401을 반환한다")
    void unauthenticatedRequestCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH-401-001"));
    }

    @Test
    @DisplayName("잘못된 토큰으로 관리자 API를 호출하면 401을 반환한다")
    void invalidTokenCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH-401-001"));
    }

    @Test
    @DisplayName("만료된 토큰으로 관리자 API를 호출하면 401을 반환한다")
    void expiredTokenCannotAccessAdminApi() throws Exception {
        Date now = new Date();
        String expiredToken = JWT.create()
                .withSubject("999")
                .withClaim("role", "ADMIN")
                .withIssuedAt(new Date(now.getTime() - 60 * 60 * 1000L))
                .withExpiresAt(new Date(now.getTime() - 1000L))
                .sign(Algorithm.HMAC256("admin-security-integration-test-secret-key"));

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH-401-001"));
    }
}
