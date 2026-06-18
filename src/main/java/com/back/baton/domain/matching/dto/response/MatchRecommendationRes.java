package com.back.baton.domain.matching.dto.response;

import java.math.BigDecimal;

public record MatchRecommendationRes(
        Long talentId,
        Long providerId,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Integer creditPrice,
        Integer estimatedHours,
        BigDecimal avgRating,
        int completeCount
) {
}