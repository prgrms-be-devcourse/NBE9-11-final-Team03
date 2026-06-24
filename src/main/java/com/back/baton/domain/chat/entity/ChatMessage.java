package com.back.baton.domain.chat.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_message",
        indexes = {
                @Index(
                        name = "idx_chat_message_room_deleted_id",
                        columnList = "room_id, deleted_at, id"
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private ChatMessageType messageType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private ChatMessage(
            ChatRoom chatRoom,
            Long senderId,
            ChatMessageType messageType,
            String content
    ) {
        this.chatRoom = chatRoom;
        this.senderId = senderId;
        this.messageType = messageType;
        this.content = content;
        this.read = false;
    }

    public static ChatMessage createTextMessage(
            ChatRoom chatRoom,
            Long senderId,
            String content
    ) {
        return new ChatMessage(
                chatRoom,
                senderId,
                ChatMessageType.TEXT,
                content
        );
    }

    public void read() {
        this.read = true;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}