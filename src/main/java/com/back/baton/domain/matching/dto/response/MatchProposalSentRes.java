package com.back.baton.domain.matching.dto.response;

import com.back.baton.domain.matching.entity.MatchProposalStatus;

import java.time.LocalDateTime;

public record MatchProposalSentRes(
        Long proposalId,
        MatchProposalStatus status,
        String requestMessage,
        Long requesterId,
        Long requesterTalentId,
        String requesterTalentTitle,
        Long providerId,
        String providerNickname,
        String providerProfileImageUrl,
        Long providerTalentId,
        String providerTalentTitle,
        LocalDateTime createdAt
) {
}