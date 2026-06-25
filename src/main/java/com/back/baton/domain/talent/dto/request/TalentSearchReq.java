package com.back.baton.domain.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "재능 검색 및 필터링 요청 DTO")
public record TalentSearchReq(
        @Schema(description = "필터링할 카테고리 ID", example = "1")
        Long categoryId,

        @Schema(description = "최소 크레딧 가격. 0 이상", example = "1000")
        @PositiveOrZero(message = "크레딧 하한값은 0 이상이어야 합니다.")
        Integer minCredit,

        @Schema(description = "최대 크레딧 가격. 0 이상", example = "10000")
        @PositiveOrZero(message = "크레딧 상한값은 0 이상이어야 합니다.")
        Integer maxCredit,

        @Schema(description = "최소 평점. 0.0 이상 5.0 이하", example = "4.0")
        @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "평점은 5.0 이하이어야 합니다.")
        BigDecimal minRating,

        @Schema(description = "완료 거래가 1건 이상 있는 재능만 조회할지 여부", example = "true")
        Boolean completedOnly
) {
}
