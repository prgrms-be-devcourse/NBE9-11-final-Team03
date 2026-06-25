package com.back.baton.domain.chat.dto.response;

import com.back.baton.domain.chat.entity.ChatRoomType;

import java.time.LocalDateTime;

public record ChatRoomListRes(
        Long roomId,
        Long tradeId,
        Long talentId,
        String talentTitle,
        Long buyerId,
        Long sellerId,
        Long opponentId,
        String opponentNickname,
        String opponentProfileImageUrl,
        String lastMessage,
        LocalDateTime lastMessageAt,
        ChatRoomType roomType,
        LocalDateTime createdAt
) {
}