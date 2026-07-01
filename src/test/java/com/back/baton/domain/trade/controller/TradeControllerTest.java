package com.back.baton.domain.trade.controller;

import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.trade.dto.response.TradeListRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.domain.trade.service.TradeSubmissionService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.exception.GlobalExceptionHandler;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.global.response.code.TradeErrorCode;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    @DisplayName("내 거래 목록을 조회하면 커서 페이지 응답을 반환한다")
    @WithMockSecurityUser(userId = 2)
    void getMyTrades_success() throws Exception {
        Long userId = 2L;
        List<TradeListRes> content = List.of(
                new TradeListRes(2L, 10L, userId, 3L, 5000, TradeType.PURCHASE, TradeStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now()),
                new TradeListRes(1L, 10L, userId, 3L, 5000, TradeType.PURCHASE, TradeStatus.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now())
        );
        CursorPageRes<TradeListRes> page = new CursorPageRes<>(content, false, null);

        when(tradeService.getMyTrades(eq(userId), isNull(), isNull(), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/v1/trade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-16"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist());
    }

    @Test
    @DisplayName("status 필터를 전달하면 해당 상태의 거래 목록만 반환한다")
    @WithMockSecurityUser(userId = 2)
    void getMyTrades_withStatusFilter() throws Exception {
        Long userId = 2L;
        List<TradeListRes> content = List.of(
                new TradeListRes(1L, 10L, userId, 3L, 5000, TradeType.PURCHASE, TradeStatus.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now())
        );
        CursorPageRes<TradeListRes> page = new CursorPageRes<>(content, false, null);

        when(tradeService.getMyTrades(eq(userId), eq(TradeStatus.IN_PROGRESS), isNull(), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/v1/trade").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].tradeStatus").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("다음 페이지가 있으면 hasNext=true이고 nextCursor가 존재한다")
    @WithMockSecurityUser(userId = 2)
    void getMyTrades_hasNext() throws Exception {
        Long userId = 2L;
        List<TradeListRes> content = List.of(
                new TradeListRes(2L, 10L, userId, 3L, 5000, TradeType.PURCHASE, TradeStatus.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now()),
                new TradeListRes(1L, 10L, userId, 3L, 5000, TradeType.PURCHASE, TradeStatus.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now())
        );
        CursorPageRes<TradeListRes> page = new CursorPageRes<>(content, true, 1L);

        when(tradeService.getMyTrades(eq(userId), isNull(), isNull(), eq(2))).thenReturn(page);

        mockMvc.perform(get("/api/v1/trade").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(1));
    }

    @Test
    @DisplayName("size가 1 미만이면 400을 반환한다")
    @WithMockSecurityUser(userId = 2)
    void getMyTrades_invalidSize_tooSmall() throws Exception {
        mockMvc.perform(get("/api/v1/trade").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.size").value("페이지 크기는 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("size가 50 초과이면 400을 반환한다")
    @WithMockSecurityUser(userId = 2)
    void getMyTrades_invalidSize_tooLarge() throws Exception {
        mockMvc.perform(get("/api/v1/trade").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.size").value("페이지 크기는 50 이하이어야 합니다."));
    }

    @Test
    @DisplayName("현재 사용자로 거래 상세 조회 시 성공한다")
    @WithMockSecurityUser(userId = 2)
    void getTrade_success() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        TradeRes res = new TradeRes(
                tradeId,
                1L,
                null,
                10L,
                2L,
                3L,
                5000,
                TradeType.PURCHASE,
                TradeStatus.IN_PROGRESS,
                EscrowStatus.HELD,
                expiresAt,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(tradeService.getMyTrade(tradeId, userId)).thenReturn(res);

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
    @DisplayName("존재하지 않는 거래를 조회하면 404를 반환한다")
    @WithMockSecurityUser(userId = 2)
    void getTrade_notFound() throws Exception {
        Long tradeId = 999L;
        Long userId = 2L;

        when(tradeService.getMyTrade(tradeId, userId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/trade/{tradeId}", tradeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-001"));
    }

    @Test
    @DisplayName("거래 참여자가 아닌 사용자가 조회하면 403을 반환한다")
    @WithMockSecurityUser(userId = 999)
    void getTrade_accessDenied() throws Exception {
        Long tradeId = 1L;
        Long outsiderId = 999L;

        when(tradeService.getMyTrade(tradeId, outsiderId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(get("/api/v1/trade/{tradeId}", tradeId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("현재 사용자로 거래 취소 시 성공한다")
    @WithMockSecurityUser(userId = 2)
    void cancelTrade_success() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;

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

        when(tradeService.cancelTrade(tradeId, userId)).thenReturn(res);

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-8"))
                .andExpect(jsonPath("$.data.tradeStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.data.escrowStatus").value("REFUNDED"));
    }

    @Test
    @DisplayName("존재하지 않는 거래를 취소하면 404를 반환한다")
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
    @DisplayName("거래 참여자가 아닌 사용자가 취소하면 403을 반환한다")
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
    @DisplayName("검토 중인 거래를 취소하면 400을 반환한다")
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
    @DisplayName("이미 완료된 거래를 취소하면 400을 반환한다")
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
    @DisplayName("이미 취소된 거래를 취소하면 409를 반환한다")
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
    @DisplayName("이미 진행된 거래를 취소하면 400을 반환한다")
    @WithMockSecurityUser(userId = 2)
    void cancelTrade_alreadyInProgress() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;

        when(tradeService.cancelTrade(tradeId, userId))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ALREADY_IN_PROGRESS));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/cancel", tradeId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-009"));
    }

    @Test
    @DisplayName("분쟁 중인 거래를 취소하면 400을 반환한다")
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

    @Test
    @DisplayName("현재 사용자로 분쟁 신청 시 성공한다")
    @WithMockSecurityUser(userId = 2)
    void disputeTrade_success() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;
        String reason = "결과물이 약속한 조건과 다릅니다.";

        TradeRes res = new TradeRes(
                tradeId,
                1L,
                null,
                10L,
                2L,
                3L,
                5000,
                TradeType.PURCHASE,
                TradeStatus.DISPUTED,
                EscrowStatus.FROZEN,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(tradeService.disputeTrade(tradeId, userId, reason)).thenReturn(res);

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/dispute", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"" + reason + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-11"))
                .andExpect(jsonPath("$.data.tradeStatus").value("DISPUTED"))
                .andExpect(jsonPath("$.data.escrowStatus").value("FROZEN"));
    }

    @Test
    @DisplayName("분쟁 사유가 공백이면 400을 반환한다")
    @WithMockSecurityUser(userId = 2)
    void disputeTrade_blankReason() throws Exception {
        Long tradeId = 1L;

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/dispute", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("검토 중이 아닌 거래에 분쟁을 신청하면 400을 반환한다")
    @WithMockSecurityUser(userId = 2)
    void disputeTrade_notUnderReview() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;
        String reason = "분쟁 사유입니다.";

        when(tradeService.disputeTrade(eq(tradeId), eq(userId), eq(reason)))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_UNDER_REVIEW));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/dispute", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"" + reason + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-005"));
    }

    @Test
    @DisplayName("이미 분쟁 중인 거래에 분쟁을 신청하면 409를 반환한다")
    @WithMockSecurityUser(userId = 2)
    void disputeTrade_alreadyDisputed() throws Exception {
        Long tradeId = 1L;
        Long userId = 2L;
        String reason = "분쟁 사유입니다.";

        when(tradeService.disputeTrade(eq(tradeId), eq(userId), eq(reason)))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ALREADY_DISPUTED));

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/dispute", tradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"" + reason + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-409-002"));
    }
}
