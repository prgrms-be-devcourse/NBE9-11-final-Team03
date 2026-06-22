// domain.category.dto.response.CategoryRes
package com.back.baton.domain.category.dto.response;

import com.back.baton.domain.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리 조회 응답 DTO")
public record CategoryRes(
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,
        @Schema(description = "카테고리 이름", example = "개발")
        String name,
        @Schema(description = "노출 순서", example = "1")
        int sortOrder
) {
    public static CategoryRes from(Category category) {
        return new CategoryRes(
                category.getId(),
                category.getName(),
                category.getSortOrder()
        );
    }
}