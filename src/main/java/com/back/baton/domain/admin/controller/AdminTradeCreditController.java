package com.back.baton.domain.admin.controller;

import com.back.baton.domain.admin.dto.request.AdminTradeSearchReq;
import com.back.baton.domain.admin.dto.response.AdminCreditAccountRes;
import com.back.baton.domain.admin.dto.response.AdminEscrowRes;
import com.back.baton.domain.admin.dto.response.AdminPageRes;
import com.back.baton.domain.admin.dto.response.AdminTradeRes;
import com.back.baton.domain.admin.service.AdminTradeCreditService;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Trade Credit", description = "관리자 거래/에스크로/크레딧 조회 API")
public class AdminTradeCreditController {

    private final AdminTradeCreditService adminTradeCreditService;

    @GetMapping("/trades")
    @Operation(summary = "관리자 거래 목록 조회")
    public ResponseEntity<ApiResponse<AdminPageRes<AdminTradeRes>>> getTrades(
            @ParameterObject @ModelAttribute AdminTradeSearchReq req,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok(adminTradeCreditService.getTrades(req, pageable));
    }

    @GetMapping("/trades/{tradeId}")
    @Operation(summary = "관리자 거래 상세 조회")
    public ResponseEntity<ApiResponse<TradeRes>> getTrade(@PathVariable Long tradeId) {
        return ApiResponses.ok(adminTradeCreditService.getTrade(tradeId));
    }

    @GetMapping("/trades/{tradeId}/escrow")
    @Operation(summary = "관리자 거래 에스크로 조회")
    public ResponseEntity<ApiResponse<AdminEscrowRes>> getEscrow(@PathVariable Long tradeId) {
        return ApiResponses.ok(adminTradeCreditService.getEscrow(tradeId));
    }

    @GetMapping("/users/{userId}/credits")
    @Operation(summary = "관리자 유저 크레딧 잔액 조회")
    public ResponseEntity<ApiResponse<AdminCreditAccountRes>> getCreditAccount(@PathVariable Long userId) {
        return ApiResponses.ok(adminTradeCreditService.getCreditAccount(userId));
    }

    @GetMapping("/users/{userId}/credit-transactions")
    @Operation(summary = "관리자 유저 크레딧 거래 내역 조회")
    public ResponseEntity<ApiResponse<AdminPageRes<CreditTransactionRes>>> getCreditTransactions(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok(adminTradeCreditService.getCreditTransactions(userId, pageable));
    }
}
