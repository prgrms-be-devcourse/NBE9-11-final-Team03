package com.back.baton.domain.credit.controller;

import com.back.baton.domain.credit.dto.request.CreditTransactionSearchReq;
import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.global.response.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            description = "사용자의 사용 가능 크레딧 balance와 에스크로 보류 크레딧 escrowBalance를 조회합니다."
    )
    public ResponseEntity<ApiResponse<CreditBalanceRes>> getBalance(
            @Parameter(description = "조회할 사용자 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "1", required = true)
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(creditService.getBalance(userId)));
    }

    @GetMapping("/transactions")
    @Operation(
            summary = "크레딧 거래 내역 조회",
            description = "사용자의 크레딧 거래 내역을 커서 기반 페이지네이션으로 조회합니다. 거래 타입과 기간으로 필터링할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<CursorPageRes<CreditTransactionRes>>> getTransactionHistory(
            @Parameter(description = "조회할 사용자 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "1", required = true)
            @RequestParam Long userId,
            @ParameterObject @ModelAttribute CreditTransactionSearchReq req,
            @Parameter(description = "마지막으로 조회한 거래 원장 ID. 첫 요청은 생략합니다.", example = "1001")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "조회할 내역 개수 (최대 50)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponses.ok(creditService.getTransactionHistory(userId, req, cursor, size));
    }
}
