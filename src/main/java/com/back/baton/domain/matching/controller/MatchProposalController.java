package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.service.MatchProposalService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/match-proposals")
@RequiredArgsConstructor
@Tag(name = "Matching / MatchProposal", description = "매칭 제안 생성, 수락, 거절 API")
public class MatchProposalController {

    private final MatchProposalService matchProposalService;

    @PostMapping
    @Operation(
            summary = "매칭 제안 생성",
            description = "요청자가 제공자에게 재능 거래를 제안합니다. 생성 시 상태는 REQUESTED입니다."
    )
    public ResponseEntity<ApiResponse<MatchProposalRes>> createMatchProposal(
            @Parameter(description = "요청자 회원 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "1", required = true)
            @RequestParam Long requesterId,
            @Valid @RequestBody MatchProposalCreateReq req
    ) {
        MatchProposalRes response = matchProposalService.createMatchProposal(requesterId, req);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_CREATED, response);
    }

    @PatchMapping("/{proposalId}/accept")
    @Operation(
            summary = "매칭 제안 수락",
            description = "제공자가 REQUESTED 상태의 매칭 제안을 수락합니다. 수락 후 상태는 ACCEPTED입니다."
    )
    public ResponseEntity<ApiResponse<MatchProposalRes>> acceptMatchProposal(
            @Parameter(description = "매칭 제안 ID", example = "1", required = true)
            @PathVariable Long proposalId,
            @Parameter(description = "제공자 회원 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "2", required = true)
            @RequestParam Long providerId
    ) {
        MatchProposalRes response = matchProposalService.acceptMatchProposal(proposalId, providerId);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_ACCEPTED, response);
    }

    @PatchMapping("/{proposalId}/reject")
    @Operation(
            summary = "매칭 제안 거절",
            description = "제공자가 REQUESTED 상태의 매칭 제안을 거절합니다. 거절 후 상태는 REJECTED입니다."
    )
    public ResponseEntity<ApiResponse<MatchProposalRes>> rejectMatchProposal(
            @Parameter(description = "매칭 제안 ID", example = "1", required = true)
            @PathVariable Long proposalId,
            @Parameter(description = "제공자 회원 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "2", required = true)
            @RequestParam Long providerId
    ) {
        MatchProposalRes response = matchProposalService.rejectMatchProposal(proposalId, providerId);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_REJECTED, response);
    }
}
