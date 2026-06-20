package com.back.baton.domain.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "재능 수정 요청 DTO")
public record TalentUpdateReq(
        @Schema(description = "재능 카테고리 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "카테고리는 필수입니다.")
        Long categoryId,

        @Schema(
                description = "재능 제목. 100자 이하",
                example = "Spring Boot API 설계 도와드립니다.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 100
        )
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Schema(
                description = "재능 상세 내용. 10000자 이하",
                example = "REST API 설계와 테스트 코드 작성을 도와드립니다.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 10000
        )
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "내용은 10000자 이하여야 합니다.")
        String content,

        @Schema(
                description = "예상 소요 시간. 1 이상",
                example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1"
        )
        @NotNull
        @Positive(message = "예상 소요 시간은 1 이상이어야 합니다.")
        Integer estimatedHours,

        @Schema(
                description = "거래에 필요한 크레딧 가격. 0 이상",
                example = "5000",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0"
        )
        @NotNull
        @PositiveOrZero(message = "크레딧 가격은 0 이상이어야 합니다.")
        Integer creditPrice
) {
}
