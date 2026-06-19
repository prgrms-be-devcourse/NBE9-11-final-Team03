package com.back.baton.domain.talent.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

// 검색 필터 4종
public record TalentSearchReq(
        Long categoryId,        // 카테고리

        @PositiveOrZero(message = "크레딧 하한선은 0 이상이어야 합니다.")
        Integer minCredit,      // 크레딧 하한

        @PositiveOrZero(message = "크레딧 상한선은 0 이상이어야 합니다.")
        Integer maxCredit,      // 크레딧 상한

        @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "평점은 5.0 이하이어야 합니다.")
        BigDecimal minRating,   // 최소 평점

        Boolean completedOnly   // 완료 1건 이상
) {}