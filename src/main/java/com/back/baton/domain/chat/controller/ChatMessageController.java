package com.back.baton.domain.chat.controller;

import com.back.baton.domain.chat.dto.request.ChatMessageCreateReq;
import com.back.baton.domain.chat.dto.response.ChatMessageRes;
import com.back.baton.domain.chat.service.ChatService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-rooms/{chatRoomId}/messages")
@RequiredArgsConstructor
@Tag(name = "Chat / ChatMessage", description = "채팅 메시지 전송 및 조회 API")
public class ChatMessageController {

    private final ChatService chatService;

    @PostMapping
    @Operation(
            summary = "채팅 메시지 전송",
            description = "채팅방 참여자가 메시지를 전송합니다. 전송된 메시지는 MySQL에 저장됩니다."
    )
    public ResponseEntity<ApiResponse<ChatMessageRes>> sendMessage(
            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ChatMessageCreateReq req
    ) {
        ChatMessageRes response = chatService.sendMessage(
                chatRoomId,
                req.senderId(),
                req.content()
        );

        return ApiResponses.success(SuccessCode.CHAT_MESSAGE_SENT, response);
    }

    @GetMapping
    @Operation(
            summary = "채팅 메시지 목록 조회",
            description = "채팅방 참여자가 특정 채팅방의 메시지 목록을 조회합니다. 메시지는 오래된 순서로 조회됩니다."
    )
    public ResponseEntity<ApiResponse<List<ChatMessageRes>>> getMessages(
            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,
            @Parameter(description = "조회 요청자 회원 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "18", required = true)
            @RequestParam Long userId
    ) {
        List<ChatMessageRes> response = chatService.getMessages(
                chatRoomId,
                userId
        );

        return ApiResponses.success(SuccessCode.CHAT_MESSAGES_FOUND, response);
    }
}