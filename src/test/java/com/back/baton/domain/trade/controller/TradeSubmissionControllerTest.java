package com.back.baton.domain.trade.controller;

import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.trade.dto.response.PresignedUrlRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.dto.response.TradeSubmissionRes;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.domain.trade.service.TradeSubmissionService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.exception.GlobalExceptionHandler;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.S3ErrorCode;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @DisplayName("구매 확정 API - 성공")
    @WithMockSecurityUser(userId = 2)
    void confirmPurchase_success() throws Exception {
        Long tradeId = 1L;
        Long buyerId = 2L;
        TradeRes res = new TradeRes(
                tradeId, 1L, 10L, buyerId, 3L,
                5000, TradeType.PURCHASE, TradeStatus.COMPLETED,
                EscrowStatus.RELEASED, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(tradeService.confirmPurchase(anyLong(), anyLong())).thenReturn(res);

        mockMvc.perform(patch("/api/v1/trade/{tradeId}/confirm", tradeId)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-10"))
                .andExpect(jsonPath("$.data.tradeStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.escrowStatus").value("RELEASED"));
    }

    @Test
    @DisplayName("구매 확정 API - 구매자가 아니면 403 반환")
    @WithMockSecurityUser(userId = 999)
    void confirmPurchase_accessDenied() throws Exception {
        when(tradeService.confirmPurchase(anyLong(), anyLong()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(patch("/api/v1/trade/1/confirm")
                        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("구매 확정 API - 검토 중이 아닌 거래이면 400 반환")
    @WithMockSecurityUser(userId = 2)
    void confirmPurchase_notUnderReview() throws Exception {
        when(tradeService.confirmPurchase(anyLong(), anyLong()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_UNDER_REVIEW));

        mockMvc.perform(patch("/api/v1/trade/1/confirm")
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-005"));
    }

    @Test
    @DisplayName("결과물 확인 API - 성공")
    @WithMockSecurityUser(userId = 2)
    void getSubmission_success() throws Exception {
        Long tradeId = 1L;
        Long buyerId = 2L;
        TradeSubmissionRes res = new TradeSubmissionRes(
                10L, 1L, "https://s3.example.com/get", "결과물 설명", LocalDateTime.now()
        );

        when(tradeSubmissionService.getSubmission(anyLong(), anyLong())).thenReturn(res);

        mockMvc.perform(get("/api/v1/trade/{tradeId}/submission", tradeId)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-9"))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.fileUrl").value("https://s3.example.com/get"))
                .andExpect(jsonPath("$.data.description").value("결과물 설명"));
    }

    @Test
    @DisplayName("결과물 확인 API - 구매자가 아니면 403 반환")
    @WithMockSecurityUser(userId = 999)
    void getSubmission_accessDenied() throws Exception {
        when(tradeSubmissionService.getSubmission(anyLong(), anyLong()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(get("/api/v1/trade/1/submission")
                        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("결과물 확인 API - 검토 중이 아닌 거래이면 400 반환")
    @WithMockSecurityUser(userId = 2)
    void getSubmission_notUnderReview() throws Exception {
        when(tradeSubmissionService.getSubmission(anyLong(), anyLong()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_UNDER_REVIEW));

        mockMvc.perform(get("/api/v1/trade/1/submission")
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-005"));
    }

    @Test
    @DisplayName("결과물 확인 API - 제출 내역이 없으면 404 반환")
    @WithMockSecurityUser(userId = 2)
    void getSubmission_submissionNotFound() throws Exception {
        when(tradeSubmissionService.getSubmission(anyLong(), anyLong()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_SUBMISSION_NOT_FOUND));

        mockMvc.perform(get("/api/v1/trade/1/submission")
                        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-002"));
    }

    @Test
    @DisplayName("Presigned URL 발급 API - 성공")
    @WithMockSecurityUser(userId = 3)
    void getPresignedUrl_success() throws Exception {
        Long tradeId = 1L;
        Long sellerId = 3L;
        PresignedUrlRes res = new PresignedUrlRes("https://s3.example.com/presigned", "trades/1/uuid.pdf");

        when(tradeSubmissionService.getPresignedUrl(anyLong(), anyLong(), anyString())).thenReturn(res);

        mockMvc.perform(post("/api/v1/trade/{tradeId}/submission/presigned-url", tradeId)
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
    @WithMockSecurityUser(userId = 3)
    void getPresignedUrl_missingFileName() throws Exception {
        mockMvc.perform(post("/api/v1/trade/1/submission/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Presigned URL 발급 API - 존재하지 않는 거래이면 404 반환")
    @WithMockSecurityUser(userId = 3)
    void getPresignedUrl_tradeNotFound() throws Exception {
        when(tradeSubmissionService.getPresignedUrl(anyLong(), anyLong(), anyString()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/trade/999/submission/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"result.pdf\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-001"));
    }

    @Test
    @DisplayName("Presigned URL 발급 API - 판매자가 아니면 403 반환")
    @WithMockSecurityUser(userId = 999)
    void getPresignedUrl_accessDenied() throws Exception {
        when(tradeSubmissionService.getPresignedUrl(anyLong(), anyLong(), anyString()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(post("/api/v1/trade/1/submission/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"result.pdf\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("Presigned URL 발급 API - 진행 중이 아닌 거래이면 400 반환")
    @WithMockSecurityUser(userId = 3)
    void getPresignedUrl_notInProgress() throws Exception {
        when(tradeSubmissionService.getPresignedUrl(anyLong(), anyLong(), anyString()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_IN_PROGRESS));

        mockMvc.perform(post("/api/v1/trade/1/submission/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\": \"result.pdf\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-004"));
    }

    @Test
    @DisplayName("결과물 제출 API - 성공")
    @WithMockSecurityUser(userId = 3)
    void submitResult_success() throws Exception {
        Long tradeId = 1L;
        Long sellerId = 3L;
        TradeSubmissionRes res = new TradeSubmissionRes(
                1L, 1L, "https://s3.example.com/get", "결과물 설명", LocalDateTime.now()
        );

        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/trade/{tradeId}/submission", tradeId)
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
    @WithMockSecurityUser(userId = 3)
    void submitResult_missingFileKey() throws Exception {
        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"\", \"description\": \"설명\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("결과물 제출 API - 존재하지 않는 거래이면 404 반환")
    @WithMockSecurityUser(userId = 3)
    void submitResult_tradeNotFound() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/trade/999/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/999/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-404-001"));
    }

    @Test
    @DisplayName("결과물 제출 API - 판매자가 아니면 403 반환")
    @WithMockSecurityUser(userId = 999)
    void submitResult_accessDenied() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED));

        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/1/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-403-001"));
    }

    @Test
    @DisplayName("결과물 제출 API - 진행 중이 아닌 거래이면 400 반환")
    @WithMockSecurityUser(userId = 3)
    void submitResult_notInProgress() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(TradeErrorCode.TRADE_NOT_IN_PROGRESS));

        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/1/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TRADE-400-004"));
    }

    @Test
    @DisplayName("결과물 제출 API - fileKey가 해당 거래 경로가 아니면 400 반환")
    @WithMockSecurityUser(userId = 3)
    void submitResult_invalidFileKey() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(S3ErrorCode.INVALID_FILE_KEY));

        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/999/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("S3-400-001"));
    }

    @Test
    @DisplayName("결과물 제출 API - 에스크로가 없으면 404 반환")
    @WithMockSecurityUser(userId = 3)
    void submitResult_escrowNotFound() throws Exception {
        when(tradeSubmissionService.submitResult(anyLong(), anyLong(), any()))
                .thenThrow(new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

        mockMvc.perform(post("/api/v1/trade/1/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileKey\": \"trades/1/uuid.pdf\", \"description\": \"설명\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ESCROW-404-001"));
    }
}
