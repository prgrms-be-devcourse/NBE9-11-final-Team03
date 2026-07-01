package com.back.baton.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "WebSocket 채팅 메시지 읽음 처리 요청 DTO")
public record ChatMessageReadReq(

        @Schema(
                description = "메시지를 읽은 회원 ID. 인증 연동 전까지 메시지 body로 전달합니다.",
                example = "23",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "읽음 처리 회원 ID는 필수입니다.")
        Long readerId
) {
}