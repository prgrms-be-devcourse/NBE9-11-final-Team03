package com.back.baton.domain.credit.controller;

import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit")
@RequiredArgsConstructor
@Tag(name = "Credit", description = "크레딧 계좌 및 잔액 조회 API")
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/balance")
    @Operation(
            summary = "크레딧 잔액 조회",
            description = "현재 로그인한 사용자의 사용 가능 크레딧과 에스크로 보류 크레딧을 조회합니다."
    )
    public ResponseEntity<ApiResponse<CreditBalanceRes>> getBalance(
            @CurrentUser SecurityUser currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(creditService.getBalance(currentUser.getUserId())));
    }
}
