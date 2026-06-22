package com.back.baton.domain.chat.controller;

import com.back.baton.domain.chat.dto.request.ChatRoomCreateReq;
import com.back.baton.domain.chat.dto.response.ChatRoomRes;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
@Tag(name = "Chat / ChatRoom", description = "채팅방 생성 및 조회 API")
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping
    @Operation(
            summary = "채팅방 생성 또는 조회",
            description = "사용자가 특정 재능 판매자와 1:1 채팅방을 생성하거나, 이미 존재하는 채팅방을 조회합니다."
    )
    public ResponseEntity<ApiResponse<ChatRoomRes>> getOrCreateChatRoom(
            @Valid @RequestBody ChatRoomCreateReq req
    ) {
        ChatRoomRes response = chatService.getOrCreateMatchRoom(
                req.talentId(),
                req.buyerId()
        );

        return ApiResponses.success(SuccessCode.CHAT_ROOM_CREATED, response);
    }

    @GetMapping("/{chatRoomId}")
    @Operation(
            summary = "채팅방 단건 조회",
            description = "채팅방 참여자가 roomId로 기존 채팅방 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<ChatRoomRes>> getChatRoom(
            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,

            @Parameter(description = "조회 요청자 회원 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "23", required = true)
            @RequestParam Long userId
    ) {
        ChatRoomRes response = chatService.getChatRoomInfo(
                chatRoomId,
                userId
        );

        return ApiResponses.success(SuccessCode.CHAT_ROOM_FOUND, response);
    }
}