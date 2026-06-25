package com.back.baton.domain.chat.controller;

import com.back.baton.domain.chat.dto.request.ChatMessageCreateReq;
import com.back.baton.domain.chat.dto.response.ChatMessageRes;
import com.back.baton.domain.chat.service.ChatService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat-rooms/{chatRoomId}/messages")
@RequiredArgsConstructor
@Tag(name = "Chat / ChatMessage", description = "채팅 메시지 전송 및 조회 API")
public class ChatMessageController {

    private final ChatService chatService;

    @PostMapping
    @Operation(
            summary = "채팅 메시지 전송",
            description = "채팅방 참여자가 메시지를 전송합니다. 전송자는 현재 로그인한 사용자로 처리됩니다."
    )
    public ResponseEntity<ApiResponse<ChatMessageRes>> sendMessage(
            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,
            @CurrentUser SecurityUser currentUser,
            @Valid @RequestBody ChatMessageCreateReq req
    ) {
        ChatMessageRes response = chatService.sendMessage(
                chatRoomId,
                currentUser.getUserId(),
                req.content()
        );

        return ApiResponses.success(SuccessCode.CHAT_MESSAGE_SENT, response);
    }

    @GetMapping
    @Operation(
            summary = "채팅 메시지 목록 조회",
            description = "현재 로그인한 채팅방 참여자가 특정 채팅방의 메시지 목록을 조회합니다. 메시지는 최신순으로 조회됩니다."
    )
    public ResponseEntity<ApiResponse<CursorPageRes<ChatMessageRes>>> getMessages(
            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,
            @CurrentUser SecurityUser currentUser,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        CursorPageRes<ChatMessageRes> response = chatService.getMessages(
                chatRoomId,
                currentUser.getUserId(),
                cursor,
                size
        );

        return ApiResponses.success(SuccessCode.CHAT_MESSAGES_FOUND, response);
    }
}
