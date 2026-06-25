package com.back.baton.domain.matching.dto.response;

import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import java.time.LocalDateTime;

public record MatchProposalRes(
        Long id,
        Long providerTalentId,
        Long requesterTalentId,
        Long requesterId,
        Long providerId,
        MatchProposalStatus status,
        String requestMessage,
        LocalDateTime respondedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MatchProposalRes from(MatchProposal matchProposal) {
        return new MatchProposalRes(
                matchProposal.getId(),
                matchProposal.getProviderTalentId(),
                matchProposal.getRequesterTalentId(),
                matchProposal.getRequesterId(),
                matchProposal.getProviderId(),
                matchProposal.getStatus(),
                matchProposal.getRequestMessage(),
                matchProposal.getRespondedAt(),
                matchProposal.getCreatedAt(),
                matchProposal.getUpdatedAt()
        );
    }
}