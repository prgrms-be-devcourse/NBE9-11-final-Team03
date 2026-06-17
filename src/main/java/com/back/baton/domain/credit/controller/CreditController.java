package com.back.baton.domain.credit.controller;

import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/balance")
    @Operation(summary = "크레딧 잔액 조회", description = "내 크레딧 잔액을 조회합니다.")
    public ResponseEntity<ApiResponse<CreditBalanceRes>> getBalance(
            @RequestParam Long userId // TODO: JWT에서 userId 추출
    ) {
        return ResponseEntity.ok(ApiResponse.success(creditService.getBalance(userId)));
    }
}