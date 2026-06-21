package com.back.baton.domain.trade.controller;

import com.back.baton.domain.trade.dto.response.PresignedUrlRes;
import com.back.baton.domain.trade.dto.response.TradeSubmissionRes;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.domain.trade.service.TradeSubmissionService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.exception.GlobalExceptionHandler;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
@Import(GlobalExceptionHandler.class)
class TradeSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeService tradeService;

    @MockitoBean
    private TradeSubmissionService tradeSubmissionService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Presigned URL 발급 API - 성공")
    void getPresignedUrl_success() throws Exception {
        Long tradeId = 1L;
        Long sellerId = 3L;
        PresignedUrlRes res = new PresignedUrlRes("https://s3.example.com/presigned", "trades/1/uuid.pdf");

        when(tradeSubmissionService.getPresignedUrl(anyLong(), anyLong(), anyString())).thenReturn(res);

        mockMvc.perform(post("/api/v1/trade/{tradeId}/submission/presigned-url", tradeId)
                        .param("sellerId", String.valueOf(sellerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"result.pdf\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("201-4"))
                .andExpect(jsonPath("$.data.presignedUrl").value("https://s3.example.com/presigned"))
                .andExpect(jsonPath("$.data.fileKey").value("trades/1/uuid.pdf"));
    }

    @Test
    @DisplayName("Presigned URL 발급 API - fileName이 없으면 400 반환")
    void getPresignedUrl_missingFileName() throws Exception {
        mockMvc.perform(post("/api/v1/trade/1/submission/presigned-url")
                        .param("sellerId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Presigned URL 발급 API - 존재하지 않는 거래이면 404 반환")
    void getPresignedUrl_tradeNotFound() throws Exception {
        when(tradeSubmissionService.getPresignedUrl(anyLong(), anyLong(), anyString()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/trade/999/submission/presigned-url")
                        .param("sellerId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"result.pdf\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-001"));
    }

    @Test
    @DisplayName("Presigned URL 발급 API - 판매자가 아니면 403 반환")
    void getPresignedUrl_accessDenied() throws Exception {
        when(tradeSubmissionService.getPresignedUrl(anyLong(), anyLong(), anyString()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(post("/api/v1/trade/1/submission/presigned-url")
                        .param("sellerId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"result.pdf\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("Presigned URL 발급 API - 진행 중이 아닌 거래이면 400 반환")
    void getPresignedUrl_notInProgress() throws Exception {
        when(tradeSubmissionService.getPresignedUrl(anyLong(), anyLong(), anyString()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_IN_PROGRESS));

        mockMvc.perform(post("/api/v1/trade/1/submission/presigned-url")
                        .param("sellerId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"result.pdf\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-004"));
    }

    @Test
    @DisplayName("결과물 제출 API - 성공")
    void submitResult_success() throws Exception {
        Long tradeId = 1L;
        Long sellerId = 3L;
        TradeSubmissionRes res = new TradeSubmissionRes(
                1L, 1L, "https://s3.example.com/get", "결과물 설명", LocalDateTime.now()
        );

        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/trade/{tradeId}/submission", tradeId)
                        .param("sellerId", String.valueOf(sellerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/1/uuid.pdf\", \"description\": \"결과물 설명\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("201-5"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.fileUrl").value("https://s3.example.com/get"))
                .andExpect(jsonPath("$.data.description").value("결과물 설명"));
    }

    @Test
    @DisplayName("결과물 제출 API - fileKey가 없으면 400 반환")
    void submitResult_missingFileKey() throws Exception {
        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .param("sellerId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"\", \"description\": \"설명\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("결과물 제출 API - 존재하지 않는 거래이면 404 반환")
    void submitResult_tradeNotFound() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/trade/999/submission")
                        .param("sellerId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/999/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-001"));
    }

    @Test
    @DisplayName("결과물 제출 API - 판매자가 아니면 403 반환")
    void submitResult_accessDenied() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .param("sellerId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/1/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("결과물 제출 API - 진행 중이 아닌 거래이면 400 반환")
    void submitResult_notInProgress() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_IN_PROGRESS));

        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .param("sellerId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/1/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-004"));
    }

    @Test
    @DisplayName("결과물 제출 API - 에스크로가 없으면 404 반환")
    void submitResult_escrowNotFound() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .param("sellerId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/1/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ESCROW-404-001"));
    }
}