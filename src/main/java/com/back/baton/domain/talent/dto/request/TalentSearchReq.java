package com.back.baton.domain.talent.dto.request;

import java.math.BigDecimal;

// 검색 필터 4종
public record TalentSearchReq(
        Long categoryId,        // 카테고리
        Integer minCredit,      // 크레딧 하한
        Integer maxCredit,      // 크레딧 상한
        BigDecimal minRating,   // 최소 평점
        Boolean completedOnly   // 완료 1건 이상
) {}