package com.back.baton.domain.trade.controller;

import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.domain.trade.service.TradeSubmissionService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.exception.GlobalExceptionHandler;
import com.back.baton.global.response.code.TradeErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.support.security.WithMockSecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
@Import(GlobalExceptionHandler.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeService tradeService;

    @MockitoBean
    private TradeSubmissionService tradeSubmissionService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("get trade succeeds with current user")
    @WithMockSecurityUser(userId = 2)
    void getTrade_success() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        TradeRes res = new TradeRes(
                tradeId, 1L, 10L, 2L, 3L,
                5000, TradeType.PURCHASE, TradeStatus.IN_PROGRESS,
                EscrowStatus.HELD, expiresAt,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(tradeService.getTrade(tradeId, userId)).thenReturn(res);

        mockMvc.perform(get("/api/v1/trade/{tradeId}", tradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-7"))
                .andExpect(jsonPath("$.data.tradeId").value(tradeId))
                .andExpect(jsonPath("$.data.buyerId").value(2L))
                .andExpect(jsonPath("$.data.sellerId").value(3L))
                .andExpect(jsonPath("$.data.creditPrice").value(5000))
                .andExpect(jsonPath("$.data.tradeType").value("PURCHASE"))
                .andExpect(jsonPath("$.data.tradeStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.escrowStatus").value("HELD"));
    }

    @Test
    @DisplayName("get trade returns 404 when trade does not exist")
    @WithMockSecurityUser(userId = 2)
    void getTrade_notFound() throws Exception {
        Long tradeId = 999L;
        Long userId = 2L;

        when(tradeService.getTrade(tradeId, userId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/trade/{tradeId}", tradeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-001"));
    }

    @Test
    @DisplayName("get trade returns 403 when current user is not a participant")
    @WithMockSecurityUser(userId = 999)
    void getTrade_accessDenied() throws Exception {
        Long tradeId = 1L;
        Long outsiderId = 999L;

        when(tradeService.getTrade(tradeId, outsiderId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(get("/api/v1/trade/{tradeId}", tradeId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("cancel trade succeeds with current user")
    @WithMockSecurityUser(userId = 2)
    void cancelTrade_success() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;

        TradeRes res = new TradeRes(
                tradeId, 1L, 10L, 2L, 3L,
                5000, TradeType.PURCHASE, TradeStatus.CANCELLED,
                EscrowStatus.REFUNDED, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(tradeService.cancelTrade(tradeId, userId)).thenReturn(res);

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-8"))
                .andExpect(jsonPath("$.data.tradeStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.data.escrowStatus").value("REFUNDED"));
    }

    @Test
    @DisplayName("cancel trade returns 404 when trade does not exist")
    @WithMockSecurityUser(userId = 2)
    void cancelTrade_notFound() throws Exception {
        Long tradeId = 999L;
        Long userId = 2L;

        when(tradeService.cancelTrade(tradeId, userId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-001"));
    }

    @Test
    @DisplayName("cancel trade returns 403 when current user is not a participant")
    @WithMockSecurityUser(userId = 999)
    void cancelTrade_accessDenied() throws Exception {
        Long tradeId = 1L;
        Long outsiderId = 999L;

        when(tradeService.cancelTrade(tradeId, outsiderId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("cancel trade returns 400 when trade is under review")
    @WithMockSecurityUser(userId = 2)
    void cancelTrade_underReview() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;

        when(tradeService.cancelTrade(tradeId, userId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_UNDER_REVIEW));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-003"));
    }

    @Test
    @DisplayName("cancel trade returns 400 when trade is already completed")
    @WithMockSecurityUser(userId = 2)
    void cancelTrade_alreadyCompleted() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;

        when(tradeService.cancelTrade(tradeId, userId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ALREADY_COMPLETED));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-001"));
    }

    @Test
    @DisplayName("cancel trade returns 409 when trade is already cancelled")
    @WithMockSecurityUser(userId = 2)
    void cancelTrade_alreadyCancelled() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;

        when(tradeService.cancelTrade(tradeId, userId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ALREADY_CANCELLED));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-409-001"));
    }

    @Test
    @DisplayName("cancel trade returns 400 when trade is in dispute")
    @WithMockSecurityUser(userId = 2)
    void cancelTrade_inDispute() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;

        when(tradeService.cancelTrade(tradeId, userId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_IN_DISPUTE));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-002"));
    }
}
