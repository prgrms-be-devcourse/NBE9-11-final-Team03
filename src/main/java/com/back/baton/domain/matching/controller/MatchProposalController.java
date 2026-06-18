package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.service.MatchProposalService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
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
public class MatchProposalController {

    private final MatchProposalService matchProposalService;

    @PostMapping
    public ResponseEntity<ApiResponse<MatchProposalRes>> createMatchProposal(
            @RequestParam Long requesterId, // TODO: 인증 연동 후 로그인 사용자 ID로 대체
            @Valid @RequestBody MatchProposalCreateReq req
    ) {
        MatchProposalRes response = matchProposalService.createMatchProposal(requesterId, req);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_CREATED, response);
    }

    @PatchMapping("/{proposalId}/accept")
    public ResponseEntity<ApiResponse<MatchProposalRes>> acceptMatchProposal(
            @PathVariable Long proposalId,
            @RequestParam Long providerId // TODO: 인증 연동 후 로그인 사용자 ID로 대체
    ) {
        MatchProposalRes response = matchProposalService.acceptMatchProposal(proposalId, providerId);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_ACCEPTED, response);
    }

    @PatchMapping("/{proposalId}/reject")
    public ResponseEntity<ApiResponse<MatchProposalRes>> rejectMatchProposal(
            @PathVariable Long proposalId,
            @RequestParam Long providerId // TODO: 인증 연동 후 로그인 사용자 ID로 대체
    ) {
        MatchProposalRes response = matchProposalService.rejectMatchProposal(proposalId, providerId);

        return ApiResponses.success(SuccessCode.MATCH_PROPOSAL_REJECTED, response);
    }
}
