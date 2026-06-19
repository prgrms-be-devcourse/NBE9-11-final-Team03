package com.back.baton.domain.trade.controller;

import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/{tradeId}")
    @Operation(summary = "거래 상태 조회", description = "거래 ID로 거래 상태 및 에스크로 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<TradeRes>> getTrade(
            @PathVariable Long tradeId,
            @RequestParam Long userId // TODO: JWT에서 userId 추출
    ) {
        TradeRes response = tradeService.getTrade(tradeId, userId);
        return ApiResponses.success(SuccessCode.TRADE_OK, response);
    }

    @PatchMapping("/{tradeId}/cancel")
    @Operation(summary = "거래 취소", description = "진행 중인 거래를 취소합니다.")
    public ResponseEntity<ApiResponse<TradeRes>> cancelTrade(
            @PathVariable Long tradeId,
            @RequestParam Long userId // TODO: JWT에서 userId 추출
    ) {
        TradeRes response = tradeService.cancelTrade(tradeId, userId);
        return ApiResponses.success(SuccessCode.TRADE_CANCELLED, response);
    }
}