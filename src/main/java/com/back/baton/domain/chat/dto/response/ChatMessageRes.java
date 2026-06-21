package com.back.baton.domain.chat.dto.response;

import com.back.baton.domain.chat.entity.ChatMessage;
import com.back.baton.domain.chat.entity.ChatMessageType;
import java.time.LocalDateTime;

public record ChatMessageRes(
        Long id,
        Long roomId,
        Long senderId,
        ChatMessageType messageType,
        String content,
        boolean read,
        LocalDateTime createdAt
) {
    public static ChatMessageRes from(ChatMessage chatMessage) {
        return new ChatMessageRes(
                chatMessage.getId(),
                chatMessage.getChatRoom().getId(),
                chatMessage.getSenderId(),
                chatMessage.getMessageType(),
                chatMessage.getContent(),
                chatMessage.isRead(),
                chatMessage.getCreatedAt()
        );
    }
}