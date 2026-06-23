package com.back.baton.domain.matching.dto.response;

import com.back.baton.domain.matching.entity.MatchProposalStatus;

import java.time.LocalDateTime;

public record MatchProposalReceivedRes(
        Long proposalId,
        Long tradeId,
        MatchProposalStatus status,
        String requestMessage,
        Long requesterId,
        String requesterNickname,
        String requesterProfileImageUrl,
        Long requesterTalentId,
        String requesterTalentTitle,
        Long providerId,
        Long providerTalentId,
        String providerTalentTitle,
        LocalDateTime createdAt
) {
}