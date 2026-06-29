package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.talent.entity.TalentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// 관리자 재능 목록 조회 필터 조건.
@Schema(description = "관리자 재능 목록 조회 필터 요청 DTO")
public record AdminTalentSearchReq(
        @Schema(description = "재능 상태 필터. 생략하면 전체 상태를 조회합니다.", example = "ACTIVE")
        TalentStatus status,

        @Positive(message = "카테고리 ID는 양수여야 합니다.")
        @Schema(description = "카테고리 ID 필터", example = "1")
        Long categoryId,

        @Size(max = 100, message = "검색어는 100자 이하로 입력해 주세요.")
        @Schema(description = "재능 제목 또는 내용 검색어", example = "spring")
        String keyword
) {
}
