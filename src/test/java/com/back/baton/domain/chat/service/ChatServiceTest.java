package com.back.baton.domain.chat.service;

import com.back.baton.domain.chat.dto.response.ChatRoomRes;
import com.back.baton.domain.chat.entity.ChatMessage;
import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.domain.chat.entity.ChatRoomType;
import com.back.baton.domain.chat.repository.ChatMessageRepository;
import com.back.baton.domain.chat.repository.ChatRoomRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("기존 활성 채팅방이 없으면 매칭 채팅방을 생성한다")
    void getOrCreateMatchRoom_createNewRoom() {
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;

        Talent talent = mock(Talent.class);
        ChatRoom savedChatRoom = ChatRoom.createForMatch(talentId, buyerId, sellerId);
        ReflectionTestUtils.setField(savedChatRoom, "id", 100L);

        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));
        given(talent.getAuthorId()).willReturn(sellerId);
        given(chatRoomRepository.findActiveRoom(talentId, buyerId, sellerId, ChatRoomType.MATCH)).willReturn(Optional.empty());
        given(chatRoomRepository.save(org.mockito.ArgumentMatchers.any(ChatRoom.class))).willReturn(savedChatRoom);

        ChatRoomRes result = chatService.getOrCreateMatchRoom(talentId, buyerId);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.talentId()).isEqualTo(talentId);
        assertThat(result.buyerId()).isEqualTo(buyerId);
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.status()).isEqualTo(ChatRoomType.MATCH);

        then(talentRepository).should().findById(talentId);
        then(chatRoomRepository).should().findActiveRoom(talentId, buyerId, sellerId, ChatRoomType.MATCH);
        then(chatRoomRepository).should().save(org.mockito.ArgumentMatchers.any(ChatRoom.class));
    }

    @Test
    @DisplayName("기존 활성 채팅방이 있으면 새로 생성하지 않고 기존 채팅방을 반환한다")
    void getOrCreateMatchRoom_returnExistingRoom() {
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;

        Talent talent = mock(Talent.class);
        ChatRoom existingChatRoom = ChatRoom.createForMatch(talentId, buyerId, sellerId);
        ReflectionTestUtils.setField(existingChatRoom, "id", 100L);

        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));
        given(talent.getAuthorId()).willReturn(sellerId);
        given(chatRoomRepository.findActiveRoom(talentId, buyerId, sellerId, ChatRoomType.MATCH)).willReturn(Optional.of(existingChatRoom));

        ChatRoomRes result = chatService.getOrCreateMatchRoom(talentId, buyerId);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.talentId()).isEqualTo(talentId);
        assertThat(result.buyerId()).isEqualTo(buyerId);
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.status()).isEqualTo(ChatRoomType.MATCH);

        then(talentRepository).should().findById(talentId);
        then(chatRoomRepository).should().findActiveRoom(talentId, buyerId, sellerId, ChatRoomType.MATCH);
        then(chatRoomRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("자기 자신의 재능에는 채팅방을 생성할 수 없다")
    void getOrCreateMatchRoom_selfChatNotAllowed() {
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 10L;

        Talent talent = mock(Talent.class);

        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));
        given(talent.getAuthorId()).willReturn(sellerId);

        assertThatThrownBy(() -> chatService.getOrCreateMatchRoom(talentId, buyerId))
                .isInstanceOf(CustomException.class);

        then(talentRepository).should().findById(talentId);
        then(chatRoomRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 재능이면 채팅방을 생성할 수 없다")
    void getOrCreateMatchRoom_talentNotFound() {
        Long talentId = 999L;
        Long buyerId = 10L;

        given(talentRepository.findById(talentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getOrCreateMatchRoom(talentId, buyerId))
                .isInstanceOf(CustomException.class);

        then(talentRepository).should().findById(talentId);
        then(chatRoomRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채팅방 참여자가 메시지를 전송하면 메시지를 저장하고 채팅방 마지막 메시지 시간을 갱신한다")
    void sendMessage() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long senderId = buyerId;
        String content = "안녕하세요";

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);
        ChatMessage savedMessage = ChatMessage.createTextMessage(chatRoom, senderId, content);
        ReflectionTestUtils.setField(savedMessage, "id", 100L);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.save(org.mockito.ArgumentMatchers.any(ChatMessage.class))).willReturn(savedMessage);

        var result = chatService.sendMessage(roomId, senderId, content);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.roomId()).isEqualTo(roomId);
        assertThat(result.senderId()).isEqualTo(senderId);
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.read()).isFalse();
        assertThat(chatRoom.getLastMessageAt()).isNotNull();

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).should().save(org.mockito.ArgumentMatchers.any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅방 참여자가 아니면 메시지를 전송할 수 없다")
    void sendMessage_accessDenied() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long nonParticipantId = 999L;
        String content = "안녕하세요";

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> chatService.sendMessage(roomId, nonParticipantId, content))
                .isInstanceOf(CustomException.class);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 채팅방이면 메시지를 전송할 수 없다")
    void sendMessage_chatRoomNotFound() {
        Long roomId = 999L;
        Long senderId = 10L;
        String content = "안녕하세요";

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage(roomId, senderId, content))
                .isInstanceOf(CustomException.class);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채팅방 참여자는 메시지 목록을 조회할 수 있다")
    void getMessages() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long userId = buyerId;

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);

        ChatMessage firstMessage = ChatMessage.createTextMessage(chatRoom, buyerId, "첫 번째 메시지");
        ReflectionTestUtils.setField(firstMessage, "id", 100L);

        ChatMessage secondMessage = ChatMessage.createTextMessage(chatRoom, sellerId, "두 번째 메시지");
        ReflectionTestUtils.setField(secondMessage, "id", 101L);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.findMessages(roomId)).willReturn(List.of(firstMessage, secondMessage));

        var result = chatService.getMessages(roomId, userId);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).id()).isEqualTo(100L);
        assertThat(result.get(0).roomId()).isEqualTo(roomId);
        assertThat(result.get(0).senderId()).isEqualTo(buyerId);
        assertThat(result.get(0).content()).isEqualTo("첫 번째 메시지");

        assertThat(result.get(1).id()).isEqualTo(101L);
        assertThat(result.get(1).roomId()).isEqualTo(roomId);
        assertThat(result.get(1).senderId()).isEqualTo(sellerId);
        assertThat(result.get(1).content()).isEqualTo("두 번째 메시지");

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).should().findMessages(roomId);
    }

    @Test
    @DisplayName("채팅방 참여자가 아니면 메시지 목록을 조회할 수 없다")
    void getMessages_accessDenied() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long nonParticipantId = 999L;

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> chatService.getMessages(roomId, nonParticipantId))
                .isInstanceOf(CustomException.class);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 채팅방이면 메시지 목록을 조회할 수 없다")
    void getMessages_chatRoomNotFound() {
        Long roomId = 999L;
        Long userId = 10L;

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessages(roomId, userId))
                .isInstanceOf(CustomException.class);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채팅방 참여자가 읽음 처리하면 상대방이 보낸 미읽음 메시지를 벌크 업데이트한다")
    void markMessagesAsRead() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long readerId = buyerId;

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);
        List<Long> unreadMessageIds = List.of(100L, 101L);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.findUnreadMessageIdsFromOtherParticipant(roomId, readerId))
                .willReturn(unreadMessageIds);

        List<Long> result = chatService.markMessagesAsRead(roomId, readerId);

        assertThat(result).containsExactly(100L, 101L);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).should()
                .findUnreadMessageIdsFromOtherParticipant(roomId, readerId);
        then(chatMessageRepository).should()
                .markAsReadByIds(unreadMessageIds);
    }

    @Test
    @DisplayName("읽음 처리할 메시지가 없으면 벌크 업데이트를 호출하지 않는다")
    void markMessagesAsRead_emptyUnreadMessages() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long readerId = buyerId;

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.findUnreadMessageIdsFromOtherParticipant(roomId, readerId))
                .willReturn(List.of());

        List<Long> result = chatService.markMessagesAsRead(roomId, readerId);

        assertThat(result).isEmpty();

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).should()
                .findUnreadMessageIdsFromOtherParticipant(roomId, readerId);
        then(chatMessageRepository).should(org.mockito.Mockito.never())
                .markAsReadByIds(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("채팅방 참여자가 아니면 읽음 처리할 수 없다")
    void markMessagesAsRead_accessDenied() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long nonParticipantId = 999L;

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> chatService.markMessagesAsRead(roomId, nonParticipantId))
                .isInstanceOf(CustomException.class);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 채팅방이면 읽음 처리할 수 없다")
    void markMessagesAsRead_chatRoomNotFound() {
        Long roomId = 999L;
        Long readerId = 10L;

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.markMessagesAsRead(roomId, readerId))
                .isInstanceOf(CustomException.class);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).shouldHaveNoInteractions();
    }

    private ChatRoom createChatRoom(Long roomId, Long buyerId, Long sellerId) {
        ChatRoom chatRoom = ChatRoom.createForMatch(
                1L,
                buyerId,
                sellerId
        );

        ReflectionTestUtils.setField(chatRoom, "id", roomId);

        return chatRoom;
    }
}