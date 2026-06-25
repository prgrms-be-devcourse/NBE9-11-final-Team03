package com.back.baton.domain.admin.controller;

import com.back.baton.domain.trade.dto.request.DisputeResolveReq;
import com.back.baton.domain.trade.dto.response.DisputeRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/trade")
@RequiredArgsConstructor
@Tag(name = "Admin Trade", description = "관리자 전용 거래 관리 API")
public class AdminTradeController {

    private final TradeService tradeService;

    @GetMapping("/disputes")
    @Operation(
            summary = "분쟁 목록 조회 [관리자 전용]",
            description = "관리자가 현재 분쟁 중인 모든 거래 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<DisputeRes>>> getDisputedTrades() {
        List<DisputeRes> response = tradeService.getDisputedTrades();
        return ApiResponses.success(SuccessCode.TRADE_DISPUTES_OK, response);
    }

    @PatchMapping("/{tradeId}/dispute/resolve")
    @Operation(
            summary = "분쟁 처리 [관리자 전용]",
            description = "관리자가 분쟁 중인 거래에 대해 판정을 내립니다. BUYER_WIN이면 구매자에게 환불, SELLER_WIN이면 판매자에게 정산됩니다."
    )
    public ResponseEntity<ApiResponse<TradeRes>> resolveDispute(
            @Parameter(description = "거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @RequestBody @Valid DisputeResolveReq req
    ) {
        TradeRes response = tradeService.resolveDispute(tradeId, req.verdict());
        return ApiResponses.success(SuccessCode.TRADE_DISPUTE_RESOLVED, response);
    }
}