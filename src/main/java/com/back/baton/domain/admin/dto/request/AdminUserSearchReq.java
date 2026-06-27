package com.back.baton.domain.admin.dto.request;

import com.back.baton.domain.user.entity.UserRole;
import com.back.baton.domain.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// 관리자 유저 목록 조회 필터 조건.
@Schema(description = "관리자 유저 목록 조회 필터 요청 DTO")
public record AdminUserSearchReq(
        @Schema(description = "유저 상태 필터. 생략하면 전체 상태를 조회합니다.", example = "ACTIVE")
        UserStatus status,

        @Schema(description = "유저 권한 필터. 생략하면 전체 권한을 조회합니다.", example = "USER")
        UserRole role,

        @Schema(description = "이메일 또는 닉네임 검색어", example = "baton")
        String keyword
) {
}
