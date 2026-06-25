package com.back.baton.domain.chat.service;

import com.back.baton.domain.chat.dto.response.ChatMessageRes;
import com.back.baton.domain.chat.dto.response.ChatRoomListRes;
import com.back.baton.domain.chat.dto.response.ChatRoomRes;
import com.back.baton.domain.chat.entity.ChatMessage;
import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.domain.chat.entity.ChatRoomType;
import com.back.baton.domain.chat.repository.ChatMessageRepository;
import com.back.baton.domain.chat.repository.ChatRoomRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.CursorPageRes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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
    @DisplayName("거래 채팅방이 없으면 TRANSACTION 채팅방을 생성한다")
    void getOrCreateTransactionRoom_createNewRoom() {
        Long tradeId = 100L;
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;

        ChatRoom savedChatRoom = ChatRoom.createForTransaction(
                talentId,
                buyerId,
                sellerId,
                tradeId
        );
        ReflectionTestUtils.setField(savedChatRoom, "id", 200L);

        given(chatRoomRepository.findByTradeId(tradeId))
                .willReturn(Optional.empty());
        given(chatRoomRepository.save(org.mockito.ArgumentMatchers.any(ChatRoom.class)))
                .willReturn(savedChatRoom);

        ChatRoomRes result = chatService.getOrCreateTransactionRoom(
                tradeId,
                talentId,
                buyerId,
                sellerId
        );

        assertThat(result.id()).isEqualTo(200L);
        assertThat(result.tradeId()).isEqualTo(tradeId);
        assertThat(result.talentId()).isEqualTo(talentId);
        assertThat(result.buyerId()).isEqualTo(buyerId);
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.status()).isEqualTo(ChatRoomType.TRANSACTION);

        then(chatRoomRepository).should().findByTradeId(tradeId);
        then(chatRoomRepository).should().save(org.mockito.ArgumentMatchers.any(ChatRoom.class));
    }

    @Test
    @DisplayName("거래 채팅방이 이미 있으면 새로 생성하지 않고 기존 채팅방을 반환한다")
    void getOrCreateTransactionRoom_returnExistingRoom() {
        Long tradeId = 100L;
        Long talentId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;

        ChatRoom existingChatRoom = ChatRoom.createForTransaction(
                talentId,
                buyerId,
                sellerId,
                tradeId
        );
        ReflectionTestUtils.setField(existingChatRoom, "id", 200L);

        given(chatRoomRepository.findByTradeId(tradeId))
                .willReturn(Optional.of(existingChatRoom));

        ChatRoomRes result = chatService.getOrCreateTransactionRoom(
                tradeId,
                talentId,
                buyerId,
                sellerId
        );

        assertThat(result.id()).isEqualTo(200L);
        assertThat(result.tradeId()).isEqualTo(tradeId);
        assertThat(result.status()).isEqualTo(ChatRoomType.TRANSACTION);

        then(chatRoomRepository).should().findByTradeId(tradeId);
        then(chatRoomRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("거래 채팅방 생성 시 구매자와 판매자가 같으면 예외가 발생한다")
    void getOrCreateTransactionRoom_selfChatNotAllowed() {
        Long tradeId = 100L;
        Long talentId = 1L;
        Long userId = 10L;

        assertThatThrownBy(() -> chatService.getOrCreateTransactionRoom(
                tradeId,
                talentId,
                userId,
                userId
        ))
                .isInstanceOf(CustomException.class);

        then(chatRoomRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("내 채팅방 목록을 커서 페이지 응답으로 반환한다")
    void getMyChatRooms() {
        Long userId = 10L;
        Long cursor = null;
        int size = 2;

        LocalDateTime now = LocalDateTime.now();

        List<ChatRoomListRes> rows = List.of(
                new ChatRoomListRes(
                        5L,
                        100L,
                        1L,
                        "Spring Boot API 구현 도와드립니다",
                        10L,
                        20L,
                        20L,
                        "판매자",
                        null,
                        "마지막 메시지 1",
                        now,
                        ChatRoomType.TRANSACTION,
                        now.minusDays(1)
                ),
                new ChatRoomListRes(
                        4L,
                        null,
                        2L,
                        "React 화면 구현 도와드립니다",
                        10L,
                        30L,
                        30L,
                        "다른 판매자",
                        null,
                        "마지막 메시지 2",
                        now.minusHours(1),
                        ChatRoomType.MATCH,
                        now.minusDays(2)
                ),
                new ChatRoomListRes(
                        3L,
                        null,
                        3L,
                        "코드 구현 도와드립니다",
                        10L,
                        40L,
                        40L,
                        "또 다른 판매자",
                        null,
                        null,
                        null,
                        ChatRoomType.MATCH,
                        now.minusDays(3)
                )
        );

        given(chatRoomRepository.findMyChatRooms(userId, cursor, size))
                .willReturn(rows);

        CursorPageRes<ChatRoomListRes> result = chatService.getMyChatRooms(
                userId,
                cursor,
                size
        );

        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content())
                .extracting(ChatRoomListRes::roomId)
                .containsExactly(5L, 4L);
        assertThat(result.nextCursor()).isEqualTo(4L);

        then(chatRoomRepository).should().findMyChatRooms(userId, cursor, size);
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
    @DisplayName("채팅방 참여자는 메시지 목록을 커서 페이지 응답으로 조회할 수 있다")
    void getMessages() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long userId = buyerId;
        Long cursor = null;
        int size = 2;

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);

        ChatMessage secondMessage = ChatMessage.createTextMessage(chatRoom, sellerId, "두 번째 메시지");
        ReflectionTestUtils.setField(secondMessage, "id", 101L);

        ChatMessage firstMessage = ChatMessage.createTextMessage(chatRoom, buyerId, "첫 번째 메시지");
        ReflectionTestUtils.setField(firstMessage, "id", 100L);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.findMessages(roomId, cursor, size))
                .willReturn(List.of(secondMessage, firstMessage));

        CursorPageRes<ChatMessageRes> result = chatService.getMessages(
                roomId,
                userId,
                cursor,
                size
        );

        assertThat(result.hasNext()).isFalse();
        assertThat(result.content()).hasSize(2);

        assertThat(result.content().get(0).id()).isEqualTo(101L);
        assertThat(result.content().get(0).roomId()).isEqualTo(roomId);
        assertThat(result.content().get(0).senderId()).isEqualTo(sellerId);
        assertThat(result.content().get(0).content()).isEqualTo("두 번째 메시지");

        assertThat(result.content().get(1).id()).isEqualTo(100L);
        assertThat(result.content().get(1).roomId()).isEqualTo(roomId);
        assertThat(result.content().get(1).senderId()).isEqualTo(buyerId);
        assertThat(result.content().get(1).content()).isEqualTo("첫 번째 메시지");

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).should().findMessages(roomId, cursor, size);
    }

    @Test
    @DisplayName("채팅방 참여자는 다음 페이지가 있는 메시지 목록을 조회할 수 있다")
    void getMessages_hasNext() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long userId = buyerId;
        Long cursor = null;
        int size = 2;

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);

        ChatMessage thirdMessage = ChatMessage.createTextMessage(chatRoom, buyerId, "세 번째 메시지");
        ReflectionTestUtils.setField(thirdMessage, "id", 102L);

        ChatMessage secondMessage = ChatMessage.createTextMessage(chatRoom, sellerId, "두 번째 메시지");
        ReflectionTestUtils.setField(secondMessage, "id", 101L);

        ChatMessage firstMessage = ChatMessage.createTextMessage(chatRoom, buyerId, "첫 번째 메시지");
        ReflectionTestUtils.setField(firstMessage, "id", 100L);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.findMessages(roomId, cursor, size))
                .willReturn(List.of(thirdMessage, secondMessage, firstMessage));

        CursorPageRes<ChatMessageRes> result = chatService.getMessages(
                roomId,
                userId,
                cursor,
                size
        );

        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content())
                .extracting(ChatMessageRes::id)
                .containsExactly(102L, 101L);
        assertThat(result.nextCursor()).isEqualTo(101L);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).should().findMessages(roomId, cursor, size);
    }

    @Test
    @DisplayName("채팅방 참여자가 아니면 메시지 목록을 조회할 수 없다")
    void getMessages_accessDenied() {
        Long roomId = 1L;
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long nonParticipantId = 999L;
        Long cursor = null;
        int size = 20;

        ChatRoom chatRoom = createChatRoom(roomId, buyerId, sellerId);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        assertThatThrownBy(() -> chatService.getMessages(roomId, nonParticipantId, cursor, size))
                .isInstanceOf(CustomException.class);

        then(chatRoomRepository).should().findById(roomId);
        then(chatMessageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 채팅방이면 메시지 목록을 조회할 수 없다")
    void getMessages_chatRoomNotFound() {
        Long roomId = 999L;
        Long userId = 10L;
        Long cursor = null;
        int size = 20;

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessages(roomId, userId, cursor, size))
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