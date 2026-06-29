package com.back.baton.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "채팅방 생성 또는 조회 요청 DTO")
public record ChatRoomCreateReq(
        @Schema(description = "채팅을 시작할 재능 ID", example = "22", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "재능 ID는 필수입니다.")
        Long talentId
) {
}