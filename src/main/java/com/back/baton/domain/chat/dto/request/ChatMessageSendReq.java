package com.back.baton.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "WebSocket 채팅 메시지 전송 요청 DTO")
public record ChatMessageSendReq(
        @NotBlank(message = "메시지 내용은 필수입니다.")
        @Size(max = 1000, message = "메시지는 1000자 이하로 입력해 주세요.")
        String content
) {
}