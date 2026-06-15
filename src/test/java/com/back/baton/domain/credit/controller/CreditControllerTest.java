package com.back.baton.domain.credit.controller;

import com.back.baton.domain.credit.dto.CreditBalanceRes;
import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreditController.class)
class CreditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreditService creditService;

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
        given(creditService.getBalance(999L)).willThrow(new CustomException(ErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/credit/balance")
                        .param("userId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("404-2"))
                .andExpect(jsonPath("$.message").value("크레딧 계좌를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}