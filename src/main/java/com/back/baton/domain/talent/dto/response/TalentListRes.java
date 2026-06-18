package com.back.baton.domain.talent.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TalentListRes(
        Long talentId,
        String categoryName,
        String title,
        Integer creditPrice,
        Integer estimatedHours,
        BigDecimal avgRating,
        int completeCount,
        int viewCount,
        LocalDateTime createdAt
) {
}