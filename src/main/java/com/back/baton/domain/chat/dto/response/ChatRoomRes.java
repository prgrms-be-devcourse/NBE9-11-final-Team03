package com.back.baton.domain.chat.dto.response;

import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.domain.chat.entity.ChatRoomType;
import java.time.LocalDateTime;

public record ChatRoomRes(
        Long id,
        Long talentId,
        Long buyerId,
        Long sellerId,
        Long tradeId,
        Long tradeGroupId,
        ChatRoomType status,
        LocalDateTime lastMessageAt,
        LocalDateTime createdAt
) {
    public static ChatRoomRes from(ChatRoom chatRoom) {
        return new ChatRoomRes(
                chatRoom.getId(),
                chatRoom.getTalentId(),
                chatRoom.getBuyerId(),
                chatRoom.getSellerId(),
                chatRoom.getTradeId(),
                chatRoom.getTradeGroupId(),
                chatRoom.getStatus(),
                chatRoom.getLastMessageAt(),
                chatRoom.getCreatedAt()
        );
    }
}