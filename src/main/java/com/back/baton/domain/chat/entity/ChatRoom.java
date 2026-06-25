package com.back.baton.domain.chat.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_room",
        indexes = {
                @Index(
                        name = "idx_chat_room_match_lookup",
                        columnList = "talent_id, buyer_id, seller_id, status, deleted_at"
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "talent_id", nullable = false)
    private Long talentId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "trade_id")
    private Long tradeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomType status;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private ChatRoom(
            Long talentId,
            Long buyerId,
            Long sellerId,
            Long tradeId,
            ChatRoomType status
    ) {
        this.talentId = talentId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.tradeId = tradeId;
        this.status = status;
    }

    public static ChatRoom createForMatch(
            Long talentId,
            Long buyerId,
            Long sellerId
    ) {
        return new ChatRoom(
                talentId,
                buyerId,
                sellerId,
                null,
                ChatRoomType.MATCH
        );
    }

    public static ChatRoom createForTransaction(
            Long talentId,
            Long buyerId,
            Long sellerId,
            Long tradeId
    ) {
        return new ChatRoom(
                talentId,
                buyerId,
                sellerId,
                tradeId,
                ChatRoomType.TRANSACTION
        );
    }

    public void updateLastMessageAt() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public boolean isParticipant(Long userId) {
        return buyerId.equals(userId) || sellerId.equals(userId);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}