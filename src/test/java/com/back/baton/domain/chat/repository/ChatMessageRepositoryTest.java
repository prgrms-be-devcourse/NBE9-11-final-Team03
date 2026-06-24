package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.entity.ChatMessage;
import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("특정 채팅방의 삭제되지 않은 메시지를 최신순으로 조회한다")
    void findMessages() {
        ChatRoom chatRoom = saveChatRoom(1L, 10L, 20L);
        ChatRoom otherChatRoom = saveChatRoom(2L, 10L, 30L);

        ChatMessage firstMessage = saveMessage(chatRoom, 10L, "첫 번째 메시지");
        ChatMessage secondMessage = saveMessage(chatRoom, 20L, "두 번째 메시지");

        ChatMessage deletedMessage = saveMessage(chatRoom, 10L, "삭제된 메시지");
        deletedMessage.delete();
        chatMessageRepository.save(deletedMessage);

        saveMessage(otherChatRoom, 30L, "다른 채팅방 메시지");

        entityManager.flush();
        entityManager.clear();

        Long cursor = null;
        int size = 20;

        List<ChatMessage> result = chatMessageRepository.findMessages(
                chatRoom.getId(),
                cursor,
                size
        );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ChatMessage::getId)
                .containsExactly(secondMessage.getId(), firstMessage.getId());
        assertThat(result).extracting(ChatMessage::getContent)
                .containsExactly("두 번째 메시지", "첫 번째 메시지");
    }

    @Test
    @DisplayName("커서가 있으면 해당 커서보다 오래된 메시지만 최신순으로 조회한다")
    void findMessages_withCursor() {
        ChatRoom chatRoom = saveChatRoom(1L, 10L, 20L);

        ChatMessage firstMessage = saveMessage(chatRoom, 10L, "첫 번째 메시지");
        ChatMessage secondMessage = saveMessage(chatRoom, 20L, "두 번째 메시지");
        ChatMessage thirdMessage = saveMessage(chatRoom, 10L, "세 번째 메시지");

        entityManager.flush();
        entityManager.clear();

        List<ChatMessage> result = chatMessageRepository.findMessages(
                chatRoom.getId(),
                thirdMessage.getId(),
                20
        );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ChatMessage::getId)
                .containsExactly(secondMessage.getId(), firstMessage.getId());
        assertThat(result)
                .extracting(ChatMessage::getContent)
                .containsExactly("두 번째 메시지", "첫 번째 메시지");
    }

    @Test
    @DisplayName("상대방이 보낸 미읽음 메시지 ID만 조회한다")
    void findUnreadMessageIdsFromOtherParticipant() {
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long readerId = buyerId;

        ChatRoom chatRoom = saveChatRoom(1L, buyerId, sellerId);
        ChatRoom otherChatRoom = saveChatRoom(2L, buyerId, 30L);

        ChatMessage unreadFromSeller1 = saveMessage(chatRoom, sellerId, "상대방 미읽음 메시지 1");
        ChatMessage unreadFromSeller2 = saveMessage(chatRoom, sellerId, "상대방 미읽음 메시지 2");

        saveMessage(chatRoom, buyerId, "내가 보낸 메시지");

        ChatMessage readFromSeller = saveMessage(chatRoom, sellerId, "이미 읽은 메시지");
        readFromSeller.read();
        chatMessageRepository.save(readFromSeller);

        ChatMessage deletedFromSeller = saveMessage(chatRoom, sellerId, "삭제된 메시지");
        deletedFromSeller.delete();
        chatMessageRepository.save(deletedFromSeller);

        saveMessage(otherChatRoom, 30L, "다른 채팅방 메시지");

        entityManager.flush();
        entityManager.clear();

        List<Long> result = chatMessageRepository.findUnreadMessageIdsFromOtherParticipant(
                chatRoom.getId(),
                readerId
        );

        assertThat(result).containsExactlyInAnyOrder(
                unreadFromSeller1.getId(),
                unreadFromSeller2.getId()
        );
    }

    @Test
    @DisplayName("전달받은 메시지 ID 목록을 읽음 처리한다")
    void markAsReadByIds() {
        Long buyerId = 10L;
        Long sellerId = 20L;

        ChatRoom chatRoom = saveChatRoom(1L, buyerId, sellerId);

        ChatMessage firstMessage = saveMessage(chatRoom, sellerId, "첫 번째 메시지");
        ChatMessage secondMessage = saveMessage(chatRoom, sellerId, "두 번째 메시지");
        ChatMessage untouchedMessage = saveMessage(chatRoom, sellerId, "읽음 처리 대상이 아닌 메시지");

        entityManager.flush();
        entityManager.clear();

        chatMessageRepository.markAsReadByIds(List.of(
                firstMessage.getId(),
                secondMessage.getId()
        ));

        entityManager.flush();
        entityManager.clear();

        List<ChatMessage> updatedMessages = chatMessageRepository.findAllById(List.of(
                firstMessage.getId(),
                secondMessage.getId()
        ));

        ChatMessage notUpdatedMessage = chatMessageRepository.findById(untouchedMessage.getId())
                .orElseThrow();

        assertThat(updatedMessages).hasSize(2);
        assertThat(updatedMessages).allMatch(ChatMessage::isRead);
        assertThat(notUpdatedMessage.isRead()).isFalse();
    }

    @Test
    @DisplayName("읽음 처리 후에는 조회되지 않는다")
    void findUnreadMessageIdsFromOtherParticipant_afterMarkAsRead() {
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long readerId = buyerId;

        ChatRoom chatRoom = saveChatRoom(1L, buyerId, sellerId);

        ChatMessage firstMessage = saveMessage(chatRoom, sellerId, "첫 번째 메시지");
        ChatMessage secondMessage = saveMessage(chatRoom, sellerId, "두 번째 메시지");

        entityManager.flush();
        entityManager.clear();

        chatMessageRepository.markAsReadByIds(List.of(
                firstMessage.getId(),
                secondMessage.getId()
        ));

        entityManager.flush();
        entityManager.clear();

        List<Long> result = chatMessageRepository.findUnreadMessageIdsFromOtherParticipant(
                chatRoom.getId(),
                readerId
        );

        assertThat(result).isEmpty();
    }

    private ChatRoom saveChatRoom(Long talentId, Long buyerId, Long sellerId) {
        ChatRoom chatRoom = ChatRoom.createForMatch(
                talentId,
                buyerId,
                sellerId
        );

        return chatRoomRepository.save(chatRoom);
    }

    private ChatMessage saveMessage(ChatRoom chatRoom, Long senderId, String content) {
        ChatMessage chatMessage = ChatMessage.createTextMessage(
                chatRoom,
                senderId,
                content
        );

        return chatMessageRepository.save(chatMessage);
    }
}