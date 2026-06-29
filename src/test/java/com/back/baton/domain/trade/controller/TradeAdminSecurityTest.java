package com.back.baton.domain.trade.controller;

import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.trade.dto.response.DisputeRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.DisputeVerdict;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.domain.trade.service.TradeSubmissionService;
import com.back.baton.global.security.SecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=trade-admin-security-test-secret-key",
        "hash.salt=trade-admin-security-test-salt"
})
@AutoConfigureMockMvc
class TradeAdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeService tradeService;

    @MockitoBean
    private TradeSubmissionService tradeSubmissionService;

    private static Authentication adminAuth() {
        SecurityUser principal = SecurityUser.of(1L, "ADMIN");
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    private static Authentication userAuth() {
        SecurityUser principal = SecurityUser.of(2L, "USER");
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    @Test
    @DisplayName("분쟁 목록 조회 - 관리자는 접근 가능하다")
    void getDisputedTrades_admin_allowed() throws Exception {
        DisputeRes dispute = new DisputeRes(
                1L,
                1L,
                null,
                10L,
                2L,
                3L,
                5000,
                TradeType.PURCHASE,
                TradeStatus.DISPUTED,
                EscrowStatus.FROZEN,
                "결과물이 약속한 조건과 다릅니다.",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(tradeService.getDisputedTrades()).thenReturn(List.of(dispute));

        mockMvc.perform(get("/api/v1/admin/trade/disputes")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("분쟁 목록 조회 - 일반 사용자는 403이 반환된다")
    void getDisputedTrades_user_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/trade/disputes")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("분쟁 목록 조회 - 비인증 요청은 401이 반환된다")
    void getDisputedTrades_unauthenticated_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/trade/disputes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("분쟁 처리 - 관리자는 접근 가능하다")
    void resolveDispute_admin_allowed() throws Exception {
        Long tradeId = 1L;
        TradeRes res = new TradeRes(
                tradeId,
                1L,
                null,
                10L,
                2L,
                3L,
                5000,
                TradeType.PURCHASE,
                TradeStatus.CANCELLED,
                EscrowStatus.REFUNDED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(tradeService.resolveDispute(eq(tradeId), eq(DisputeVerdict.BUYER_WIN))).thenReturn(res);

        mockMvc.perform(patch("/api/v1/admin/trade/{tradeId}/dispute/resolve", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verdict\":\"BUYER_WIN\"}")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("분쟁 처리 - 일반 사용자는 403이 반환된다")
    void resolveDispute_user_forbidden() throws Exception {
        Long tradeId = 1L;

        mockMvc.perform(patch("/api/v1/admin/trade/{tradeId}/dispute/resolve", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verdict\":\"BUYER_WIN\"}")
                        .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("분쟁 처리 - 비인증 요청은 401이 반환된다")
    void resolveDispute_unauthenticated_unauthorized() throws Exception {
        Long tradeId = 1L;

        mockMvc.perform(patch("/api/v1/admin/trade/{tradeId}/dispute/resolve", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verdict\":\"BUYER_WIN\"}"))
                .andExpect(status().isUnauthorized());
    }
}
