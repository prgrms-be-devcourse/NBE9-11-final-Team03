package com.back.baton.domain.matching.dto.response;

import java.math.BigDecimal;

public record MatchRecommendationDetailRes(
        Long talentId,
        Long requesterTalentId,
        Long providerId,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Integer creditPrice,
        Integer estimatedHours,
        BigDecimal avgRating,
        int completeCount,
        int viewCount,

        String nickname,
        String introduction,
        String profileImageUrl,
        BigDecimal trustScore,

        boolean proposalRequestEnabled,
        String proposalRequestDisabledReason
) {
    public MatchRecommendationDetailRes withProposalDisabled(String reason) {
        return new MatchRecommendationDetailRes(
                talentId,
                requesterTalentId,
                providerId,
                categoryId,
                categoryName,
                title,
                content,
                creditPrice,
                estimatedHours,
                avgRating,
                completeCount,
                viewCount,
                nickname,
                introduction,
                profileImageUrl,
                trustScore,
                false,
                reason
        );
    }
}
