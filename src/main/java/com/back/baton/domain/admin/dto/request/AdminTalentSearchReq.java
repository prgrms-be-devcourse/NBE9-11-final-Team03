package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.talent.entity.TalentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 관리자 재능 목록 조회 필터 조건.
@Schema(description = "관리자 재능 목록 조회 필터 요청 DTO")
public record AdminTalentSearchReq(
        @Schema(description = "재능 상태 필터. 생략하면 전체 상태를 조회합니다.", example = "ACTIVE")
        TalentStatus status,

        @Schema(description = "카테고리 ID 필터", example = "1")
        Long categoryId,

        @Schema(description = "재능 제목 또는 내용 검색어", example = "spring")
        String keyword
) {
}
