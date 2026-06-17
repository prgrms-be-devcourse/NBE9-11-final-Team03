package com.back.baton.domain.talent.dto.request;

import jakarta.validation.constraints.*;

public record TalentUpdateReq(
        @NotNull(message = "카테고리는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "제목은 10000자 이하여야 합니다.")
        String content,

        @NotNull @Positive(message = "예상 소요 시간은 1 이상이어야 합니다.")
        Integer estimatedHours,

        @NotNull @PositiveOrZero(message = "크레딧 가격은 0 이상이어야 합니다.")
        Integer creditPrice
) {}