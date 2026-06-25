package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.domain.chat.entity.ChatRoomType;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("findActiveRoom - talentId, buyerId, sellerId, status가 일치하는 활성 채팅방을 조회한다")
    void findActiveRoom() {
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;

        ChatRoom chatRoom = ChatRoom.createForMatch(talentId, buyerId, sellerId);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        Optional<ChatRoom> result = chatRoomRepository.findActiveRoom(
                talentId,
                buyerId,
                sellerId,
                ChatRoomType.MATCH
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedChatRoom.getId());
        assertThat(result.get().getTalentId()).isEqualTo(talentId);
        assertThat(result.get().getBuyerId()).isEqualTo(buyerId);
        assertThat(result.get().getSellerId()).isEqualTo(sellerId);
        assertThat(result.get().getStatus()).isEqualTo(ChatRoomType.MATCH);
    }

    @Test
    @DisplayName("findActiveRoom - 삭제된 채팅방은 조회하지 않는다")
    void findActiveRoom_excludeDeletedRoom() {
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;

        ChatRoom chatRoom = ChatRoom.createForMatch(talentId, buyerId, sellerId);
        chatRoom.delete();
        chatRoomRepository.save(chatRoom);

        Optional<ChatRoom> result = chatRoomRepository.findActiveRoom(
                talentId,
                buyerId,
                sellerId,
                ChatRoomType.MATCH
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findActiveRoom - status가 다르면 조회하지 않는다")
    void findActiveRoom_statusMismatch() {
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long tradeId = 100L;

        ChatRoom chatRoom = ChatRoom.createForTransaction(
                talentId,
                buyerId,
                sellerId,
                tradeId
        );
        chatRoomRepository.save(chatRoom);

        Optional<ChatRoom> result = chatRoomRepository.findActiveRoom(
                talentId,
                buyerId,
                sellerId,
                ChatRoomType.MATCH
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findActiveRoom - buyerId 또는 sellerId가 다르면 조회하지 않는다")
    void findActiveRoom_participantMismatch() {
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;

        ChatRoom chatRoom = ChatRoom.createForMatch(talentId, buyerId, sellerId);
        chatRoomRepository.save(chatRoom);

        Optional<ChatRoom> result = chatRoomRepository.findActiveRoom(
                talentId,
                999L,
                sellerId,
                ChatRoomType.MATCH
        );

        assertThat(result).isEmpty();
    }
}