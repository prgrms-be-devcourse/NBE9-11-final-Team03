package com.back.baton.domain.trade.controller;

import com.back.baton.domain.trade.dto.request.PresignedUrlReq;
import com.back.baton.domain.trade.dto.request.TradeSubmissionReq;
import com.back.baton.domain.trade.dto.response.PresignedUrlRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.dto.response.TradeSubmissionRes;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.domain.trade.service.TradeSubmissionService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
@Tag(name = "Trade", description = "거래 상태, 결과물 제출 및 구매 확정 API")
public class TradeController {

    private final TradeService tradeService;
    private final TradeSubmissionService tradeSubmissionService;

    @GetMapping("/{tradeId}")
    @Operation(
            summary = "거래 상태 조회",
            description = "현재 로그인한 거래 참여자가 거래 상태와 에스크로 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<TradeRes>> getTrade(
            @Parameter(description = "거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @CurrentUser SecurityUser currentUser
    ) {
        Long userId = currentUser.getUserId();
        TradeRes response = tradeService.getTrade(tradeId, userId);
        return ApiResponses.success(SuccessCode.TRADE_OK, response);
    }

    @PatchMapping("/{tradeId}/cancel")
    @Operation(
            summary = "거래 취소",
            description = "현재 로그인한 거래 참여자가 진행 중인 거래를 취소합니다."
    )
    public ResponseEntity<ApiResponse<TradeRes>> cancelTrade(
            @Parameter(description = "거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @CurrentUser SecurityUser currentUser
    ) {
        Long userId = currentUser.getUserId();
        TradeRes response = tradeService.cancelTrade(tradeId, userId);
        return ApiResponses.success(SuccessCode.TRADE_CANCELLED, response);
    }

    @PatchMapping("/{tradeId}/confirm")
    @Operation(
            summary = "구매 확정",
            description = "현재 로그인한 구매자가 결과물을 확인 후 구매를 확정합니다. 에스크로가 해제되고 판매자에게 크레딧이 지급됩니다."
    )
    public ResponseEntity<ApiResponse<TradeRes>> confirmPurchase(
            @Parameter(description = "거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @CurrentUser SecurityUser currentUser
    ) {
        Long buyerId = currentUser.getUserId();
        TradeRes response = tradeSubmissionService.confirmPurchase(tradeId, buyerId);
        return ApiResponses.success(SuccessCode.TRADE_COMPLETED, response);
    }

    @GetMapping("/{tradeId}/submission")
    @Operation(
            summary = "결과물 확인",
            description = "현재 로그인한 구매자가 판매자가 제출한 결과물을 조회합니다."
    )
    public ResponseEntity<ApiResponse<TradeSubmissionRes>> getSubmission(
            @Parameter(description = "거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @CurrentUser SecurityUser currentUser
    ) {
        Long buyerId = currentUser.getUserId();
        TradeSubmissionRes response = tradeSubmissionService.getSubmission(tradeId, buyerId);
        return ApiResponses.success(SuccessCode.TRADE_SUBMISSION_OK, response);
    }

    @PostMapping("/{tradeId}/submission/presigned-url")
    @Operation(
            summary = "결과물 업로드 URL 발급",
            description = "현재 로그인한 판매자가 결과물을 S3에 업로드하기 위한 presigned PUT URL을 발급합니다."
    )
    public ResponseEntity<ApiResponse<PresignedUrlRes>> getPresignedUrl(
            @Parameter(description = "거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @CurrentUser SecurityUser currentUser,
            @RequestBody @Valid PresignedUrlReq req
    ) {
        Long sellerId = currentUser.getUserId();
        PresignedUrlRes response = tradeSubmissionService.getPresignedUrl(tradeId, sellerId, req.fileName());
        return ApiResponses.success(SuccessCode.TRADE_PRESIGNED_URL_CREATED, response);
    }

    @PostMapping("/{tradeId}/submission")
    @Operation(
            summary = "결과물 제출",
            description = "현재 로그인한 판매자가 S3에 업로드한 결과물을 제출하고 거래 상태를 검토 중으로 변경합니다."
    )
    public ResponseEntity<ApiResponse<TradeSubmissionRes>> submitResult(
            @Parameter(description = "거래 ID", example = "1", required = true)
            @PathVariable Long tradeId,
            @CurrentUser SecurityUser currentUser,
            @RequestBody @Valid TradeSubmissionReq req
    ) {
        Long sellerId = currentUser.getUserId();
        TradeSubmissionRes response = tradeSubmissionService.submitResult(tradeId, sellerId, req);
        return ApiResponses.success(SuccessCode.TRADE_SUBMISSION_CREATED, response);
    }
}
