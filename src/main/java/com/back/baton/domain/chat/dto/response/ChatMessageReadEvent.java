package com.back.baton.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "WebSocket 채팅 메시지 읽음 처리 이벤트 DTO")
public record ChatMessageReadEvent(

        @Schema(description = "채팅방 ID", example = "7")
        Long roomId,

        @Schema(description = "메시지를 읽은 회원 ID", example = "23")
        Long readerId,

        @Schema(description = "읽음 처리된 메시지 ID 목록")
        List<Long> messageIds
) {
}