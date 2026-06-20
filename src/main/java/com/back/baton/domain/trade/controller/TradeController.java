package com.back.baton.domain.trade.controller;

import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Trade", description = "거래 상태 조회 및 취소 API")
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/{tradeId}")
    @Operation(
            summary = "거래 상태 조회",
            description = "거래 ID로 거래 상태와 에스크로 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<TradeRes>> getTrade(
            @Parameter(description = "조회할 거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @Parameter(description = "조회 요청 사용자 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "1", required = true)
            @RequestParam Long userId
    ) {
        TradeRes response = tradeService.getTrade(tradeId, userId);
        return ApiResponses.success(SuccessCode.TRADE_OK, response);
    }

    @PatchMapping("/{tradeId}/cancel")
    @Operation(
            summary = "거래 취소",
            description = "진행 중인 거래를 취소하고 에스크로에 보류된 크레딧을 구매자에게 환불합니다."
    )
    public ResponseEntity<ApiResponse<TradeRes>> cancelTrade(
            @Parameter(description = "취소할 거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @Parameter(description = "취소 요청 사용자 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "1", required = true)
            @RequestParam Long userId
    ) {
        TradeRes response = tradeService.cancelTrade(tradeId, userId);
        return ApiResponses.success(SuccessCode.TRADE_CANCELLED, response);
    }
}
