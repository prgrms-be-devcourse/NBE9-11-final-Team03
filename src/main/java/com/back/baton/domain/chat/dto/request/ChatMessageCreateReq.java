package com.back.baton.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "채팅 메시지 전송 요청 DTO")
public record ChatMessageCreateReq(

        @Schema(description = "메시지 발신자 회원 ID. 인증 연동 전까지 요청 바디로 전달합니다.", example = "18", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "발신자 회원 ID는 필수입니다.")
        Long senderId,

        @Schema(
                description = "채팅 메시지 내용. 1000자 이하",
                example = "안녕하세요. 상담 가능할까요?",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 1000
        )
        @NotBlank(message = "메시지 내용은 필수입니다.")
        @Size(max = 1000, message = "메시지는 1000자 이하로 입력해 주세요.")
        String content
) {
}