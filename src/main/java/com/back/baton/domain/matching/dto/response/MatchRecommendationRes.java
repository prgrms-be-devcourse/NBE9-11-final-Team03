package com.back.baton.domain.matching.dto.response;

import java.math.BigDecimal;

public record MatchRecommendationRes(
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
        boolean proposalRequestEnabled,
        String proposalRequestDisabledReason
) {
}
