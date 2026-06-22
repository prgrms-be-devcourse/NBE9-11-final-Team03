package com.back.baton.domain.credit.controller;

import com.back.baton.domain.credit.dto.request.CreditTransactionSearchReq;
import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.global.response.code.CreditErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreditController.class)
class CreditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreditService creditService;

    @MockitoBean // 또는 @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("크레딧 계좌가 존재하면 200과 잔액 정보를 반환한다")
    void getBalance_success() throws Exception {
        CreditBalanceRes response = new CreditBalanceRes(1L, 10000, 0);
        given(creditService.getBalance(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/credit/balance")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-0"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.balance").value(10000))
                .andExpect(jsonPath("$.data.escrowBalance").value(0));
    }

    @Test
    @DisplayName("크레딧 계좌가 없으면 404와 CREDIT_ACCOUNT_NOT_FOUND 에러를 반환한다")
    void getBalance_notFound() throws Exception {
        given(creditService.getBalance(999L)).willThrow(new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/credit/balance")
                        .param("userId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CREDIT-404-001"))
                .andExpect(jsonPath("$.message").value("크레딧 계좌를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("거래 내역 조회 시 200과 커서 페이지 데이터를 반환한다")
    void getTransactionHistory_success() throws Exception {
        CreditTransactionRes tx = new CreditTransactionRes(
                1005L, 100L, CreditTransactionType.REFUND, 5000, 13000,
                CreditTransactionType.REFUND.getDefaultReason(), "판매자 사정으로 환불",
                LocalDateTime.of(2026, 6, 20, 14, 0));
        CursorPageRes<CreditTransactionRes> page = CursorPageRes.of(List.of(tx), false, 1005L);
        given(creditService.getTransactionHistory(eq(1L), any(), any(), anyInt())).willReturn(page);

        mockMvc.perform(get("/api/v1/credit/transactions")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-0"))
                .andExpect(jsonPath("$.data.content[0].transactionId").value(1005))
                .andExpect(jsonPath("$.data.content[0].type").value("REFUND"))
                .andExpect(jsonPath("$.data.content[0].amount").value(5000))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(1005));
    }

    @Test
    @DisplayName("type, from, to, cursor, size 파라미터가 서비스로 그대로 바인딩된다")
    void getTransactionHistory_bindsFilterParams() throws Exception {
        given(creditService.getTransactionHistory(eq(1L), any(), any(), anyInt()))
                .willReturn(CursorPageRes.of(List.of(), false, null));

        mockMvc.perform(get("/api/v1/credit/transactions")
                        .param("userId", "1")
                        .param("type", "REFUND")
                        .param("from", "2026-06-05T00:00:00")
                        .param("to", "2026-06-20T23:59:59")
                        .param("cursor", "1005")
                        .param("size", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<CreditTransactionSearchReq> reqCaptor =
                ArgumentCaptor.forClass(CreditTransactionSearchReq.class);
        ArgumentCaptor<Long> cursorCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        then(creditService).should()
                .getTransactionHistory(eq(1L), reqCaptor.capture(), cursorCaptor.capture(), sizeCaptor.capture());

        CreditTransactionSearchReq captured = reqCaptor.getValue();
        assertThat(captured.type()).isEqualTo(CreditTransactionType.REFUND);
        assertThat(captured.from()).isEqualTo(LocalDateTime.of(2026, 6, 5, 0, 0, 0));
        assertThat(captured.to()).isEqualTo(LocalDateTime.of(2026, 6, 20, 23, 59, 59));
        assertThat(cursorCaptor.getValue()).isEqualTo(1005L);
        assertThat(sizeCaptor.getValue()).isEqualTo(10);
    }
}