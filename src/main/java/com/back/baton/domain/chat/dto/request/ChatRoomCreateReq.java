package com.back.baton.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "채팅방 생성 또는 조회 요청 DTO")
public record ChatRoomCreateReq(

        @Schema(description = "채팅을 시작할 재능 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "재능 ID는 필수입니다.")
        Long talentId,

        @Schema(description = "구매자 회원 ID. 인증 연동 전까지 요청 바디로 전달합니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "구매자 회원 ID는 필수입니다.")
        Long buyerId,

        @Schema(description = "판매자 회원 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "판매자 회원 ID는 필수입니다.")
        Long sellerId
) {
}