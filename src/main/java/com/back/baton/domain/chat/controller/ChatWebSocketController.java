package com.back.baton.domain.chat.controller;

import com.back.baton.domain.chat.dto.request.ChatMessageSendReq;
import com.back.baton.domain.chat.dto.response.ChatMessageReadEvent;
import com.back.baton.domain.chat.dto.response.ChatMessageRes;
import com.back.baton.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Validated
@Tag(name = "Chat / WebSocket", description = "WebSocket 기반 채팅 메시지 송수신 API")
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat-rooms/{chatRoomId}/messages")
    @Operation(
            summary = "WebSocket 채팅 메시지 전송",
            description = """
                    STOMP publish 경로로 전달된 채팅 메시지를 저장하고,
                    해당 채팅방을 구독 중인 사용자들에게 메시지를 실시간으로 전송합니다.
                    
                    publish: /app/chat-rooms/{chatRoomId}/messages
                    subscribe: /topic/chat-rooms/{chatRoomId}
                    """
    )
    public void sendMessage(
            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @DestinationVariable Long chatRoomId,
            Principal principal,
            @Valid @Payload ChatMessageSendReq req
    ) {
        Long senderId = Long.valueOf(principal.getName());

        ChatMessageRes response = chatService.sendMessage(
                chatRoomId,
                senderId,
                req.content()
        );

        messagingTemplate.convertAndSend(
                "/topic/chat-rooms/" + chatRoomId,
                response
        );
    }

    @MessageMapping("/chat-rooms/{chatRoomId}/read")
    @Operation(
            summary = "WebSocket 채팅 메시지 읽음 처리",
            description = """
                STOMP publish 경로로 전달된 읽음 이벤트를 처리하고,
                해당 채팅방을 구독 중인 사용자들에게 읽음 처리 결과를 실시간으로 전송합니다.

                publish: /app/chat-rooms/{chatRoomId}/read
                subscribe: /topic/chat-rooms/{chatRoomId}/read
                """
    )
    public void markAsRead(
            @Parameter(description = "채팅방 ID", example = "7", required = true)
            @DestinationVariable Long chatRoomId,
            Principal principal
    ) {
        Long readerId = Long.valueOf(principal.getName());

        List<Long> readMessageIds = chatService.markMessagesAsRead(
                chatRoomId,
                readerId
        );

        if (readMessageIds.isEmpty()) {
            return;
        }

        messagingTemplate.convertAndSend(
                "/topic/chat-rooms/" + chatRoomId + "/read",
                new ChatMessageReadEvent(
                        chatRoomId,
                        readerId,
                        readMessageIds
                )
        );
    }
}