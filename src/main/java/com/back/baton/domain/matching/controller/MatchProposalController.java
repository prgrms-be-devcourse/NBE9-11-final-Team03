package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalReceivedRes;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.dto.response.MatchProposalSentRes;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.service.MatchProposalService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/match-proposals")
@RequiredArgsConstructor
@Tag(name = "Matching / MatchProposal", description = "매칭 제안 생성, 수락, 거절, 목록 조회 API")
public class MatchProposalController {

    private final MatchProposalService matchProposalService;

    @PostMapping
    @Operation(
            summary = "매칭 제안 생성",
            description = "현재 로그인한 사용자가 제공자에게 재능 거래를 제안합니다. 생성 시 상태는 REQUESTED입니다."
    )
    public ResponseEntity<ApiResponse<MatchProposalRes>> createMatchProposal(
            @CurrentUser SecurityUser currentUser,
            @Valid @RequestBody MatchProposalCreateReq req
    ) {
        Long requesterId = currentUser.getUserId();
        MatchProposalRes response = matchProposalService.createMatchProposal(requesterId, req);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_CREATED, response);
    }

    @GetMapping("/received")
    @Operation(
            summary = "받은 매칭 제안 목록 조회",
            description = "현재 로그인한 사용자가 제공자인 매칭 제안 목록을 최신순으로 조회합니다. status 값으로 상태 필터링이 가능합니다."
    )
    public ResponseEntity<ApiResponse<List<MatchProposalReceivedRes>>> getReceivedProposals(
            @CurrentUser SecurityUser currentUser,
            @Parameter(description = "매칭 제안 상태", example = "REQUESTED")
            @RequestParam(required = false) MatchProposalStatus status
    ) {
        Long providerId = currentUser.getUserId();
        List<MatchProposalReceivedRes> response = matchProposalService.getReceivedProposals(providerId, status);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSALS_RECEIVED_FOUND, response);
    }

    @GetMapping("/sent")
    @Operation(
            summary = "보낸 매칭 제안 목록 조회",
            description = "현재 로그인한 사용자가 요청자인 매칭 제안 목록을 최신순으로 조회합니다. status 값으로 상태 필터링이 가능합니다."
    )
    public ResponseEntity<ApiResponse<List<MatchProposalSentRes>>> getSentProposals(
            @CurrentUser SecurityUser currentUser,
            @Parameter(description = "매칭 제안 상태", example = "REQUESTED")
            @RequestParam(required = false) MatchProposalStatus status
    ) {
        Long requesterId = currentUser.getUserId();
        List<MatchProposalSentRes> response = matchProposalService.getSentProposals(requesterId, status);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSALS_SENT_FOUND, response);
    }

    @PatchMapping("/{proposalId}/accept")
    @Operation(
            summary = "매칭 제안 수락",
            description = "현재 로그인한 제공자가 REQUESTED 상태의 매칭 제안을 수락합니다. 수락 후 상태는 ACCEPTED입니다."
    )
    public ResponseEntity<ApiResponse<MatchProposalRes>> acceptMatchProposal(
            @Parameter(description = "매칭 제안 ID", example = "1", required = true)
            @PathVariable Long proposalId,
            @Parameter(description = "중복 수락 요청 방지를 위한 멱등키", example = "accept-proposal-1", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @CurrentUser SecurityUser currentUser
    ) {
        Long providerId = currentUser.getUserId();
        MatchProposalRes response = matchProposalService.acceptMatchProposal(
                proposalId,
                providerId,
                idempotencyKey
        );

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_ACCEPTED, response);
    }

    @PatchMapping("/{proposalId}/reject")
    @Operation(
            summary = "매칭 제안 거절",
            description = "현재 로그인한 제공자가 REQUESTED 상태의 매칭 제안을 거절합니다. 거절 후 상태는 REJECTED입니다."
    )
    public ResponseEntity<ApiResponse<MatchProposalRes>> rejectMatchProposal(
            @Parameter(description = "매칭 제안 ID", example = "1", required = true)
            @PathVariable Long proposalId,
            @CurrentUser SecurityUser currentUser
    ) {
        Long providerId = currentUser.getUserId();
        MatchProposalRes response = matchProposalService.rejectMatchProposal(proposalId, providerId);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_REJECTED, response);
    }
}