package com.back.baton.domain.admin.controller;

import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.trade.dto.response.DisputeRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.DisputeVerdict;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.global.exception.GlobalExceptionHandler;
import com.back.baton.global.response.code.TradeErrorCode;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.support.security.WithMockSecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTradeController.class)
@Import(GlobalExceptionHandler.class)
class AdminTradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeService tradeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("분쟁 처리 - BUYER_WIN 판정 시 성공한다")
    @WithMockSecurityUser(userId = 1, role = "ADMIN")
    void resolveDispute_buyerWin() throws Exception {
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
                        .content("{\"verdict\":\"BUYER_WIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-16"))
                .andExpect(jsonPath("$.data.tradeStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.data.escrowStatus").value("REFUNDED"));
    }

    @Test
    @DisplayName("분쟁 처리 - SELLER_WIN 판정 시 성공한다")
    @WithMockSecurityUser(userId = 1, role = "ADMIN")
    void resolveDispute_sellerWin() throws Exception {
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
                TradeStatus.COMPLETED,
                EscrowStatus.RELEASED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(tradeService.resolveDispute(eq(tradeId), eq(DisputeVerdict.SELLER_WIN))).thenReturn(res);

        mockMvc.perform(patch("/api/v1/admin/trade/{tradeId}/dispute/resolve", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verdict\":\"SELLER_WIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-16"))
                .andExpect(jsonPath("$.data.tradeStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.escrowStatus").value("RELEASED"));
    }

    @Test
    @DisplayName("분쟁 처리 - 존재하지 않는 거래이면 404가 반환된다")
    @WithMockSecurityUser(userId = 1, role = "ADMIN")
    void resolveDispute_tradeNotFound() throws Exception {
        Long tradeId = 999L;

        when(tradeService.resolveDispute(eq(tradeId), any()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/admin/trade/{tradeId}/dispute/resolve", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verdict\":\"BUYER_WIN\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-001"));
    }

    @Test
    @DisplayName("분쟁 처리 - 분쟁 상태가 아닌 거래이면 400이 반환된다")
    @WithMockSecurityUser(userId = 1, role = "ADMIN")
    void resolveDispute_tradeNotDisputed() throws Exception {
        Long tradeId = 1L;

        when(tradeService.resolveDispute(eq(tradeId), any()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_DISPUTED));

        mockMvc.perform(patch("/api/v1/admin/trade/{tradeId}/dispute/resolve", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verdict\":\"SELLER_WIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-006"));
    }

    @Test
    @DisplayName("분쟁 처리 - 판정값이 null이면 400이 반환된다")
    @WithMockSecurityUser(userId = 1, role = "ADMIN")
    void resolveDispute_nullVerdict() throws Exception {
        Long tradeId = 1L;

        mockMvc.perform(patch("/api/v1/admin/trade/{tradeId}/dispute/resolve", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verdict\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("분쟁 목록 조회 - 관리자는 분쟁 목록을 조회할 수 있다")
    @WithMockSecurityUser(userId = 1, role = "ADMIN")
    void getDisputedTrades_success() throws Exception {
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

        mockMvc.perform(get("/api/v1/admin/trade/disputes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-17"))
                .andExpect(jsonPath("$.data[0].tradeId").value(1L))
                .andExpect(jsonPath("$.data[0].tradeStatus").value("DISPUTED"))
                .andExpect(jsonPath("$.data[0].escrowStatus").value("FROZEN"))
                .andExpect(jsonPath("$.data[0].disputeReason").value("결과물이 약속한 조건과 다릅니다."));
    }

    @Test
    @DisplayName("분쟁 목록 조회 - 분쟁 중인 거래가 없으면 빈 리스트가 반환된다")
    @WithMockSecurityUser(userId = 1, role = "ADMIN")
    void getDisputedTrades_noDisputes() throws Exception {
        when(tradeService.getDisputedTrades()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/trade/disputes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}