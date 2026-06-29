package com.back.baton.domain.chat.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.chat.dto.response.ChatRoomListRes;
import com.back.baton.domain.chat.entity.ChatMessage;
import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.domain.chat.entity.ChatRoomType;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private EntityManager entityManager;

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

    @Test
    @DisplayName("findMyChatRooms - 내 채팅방만 조회하고 상대방 정보와 마지막 메시지를 함께 조회한다")
    void findMyChatRooms() {
        User buyer = saveUser("buyer@test.com", "구매자");
        User seller = saveUser("seller@test.com", "판매자");
        User otherUser = saveUser("other@test.com", "다른사용자");

        Category category = categoryRepository.save(Category.create("디자인", 1));
        Talent talent = talentRepository.save(Talent.create(
                seller.getId(),
                category,
                "로고 디자인",
                "로고 디자인을 도와드립니다.",
                3,
                100
        ));

        ChatRoom myChatRoom = chatRoomRepository.save(ChatRoom.createForTransaction(
                talent.getId(),
                buyer.getId(),
                seller.getId(),
                100L
        ));

        chatMessageRepository.save(ChatMessage.createTextMessage(
                myChatRoom,
                buyer.getId(),
                "첫 번째 메시지"
        ));
        ChatMessage lastMessage = chatMessageRepository.save(ChatMessage.createTextMessage(
                myChatRoom,
                seller.getId(),
                "마지막 메시지"
        ));

        ChatRoom otherChatRoom = chatRoomRepository.save(ChatRoom.createForMatch(
                talent.getId(),
                otherUser.getId(),
                seller.getId()
        ));
        chatMessageRepository.save(ChatMessage.createTextMessage(
                otherChatRoom,
                seller.getId(),
                "다른 채팅방 메시지"
        ));

        entityManager.flush();
        entityManager.clear();

        List<ChatRoomListRes> result = chatRoomRepository.findMyChatRooms(
                buyer.getId(),
                null,
                20
        );

        assertThat(result).hasSize(1);

        ChatRoomListRes response = result.get(0);
        assertThat(response.roomId()).isEqualTo(myChatRoom.getId());
        assertThat(response.tradeId()).isEqualTo(100L);
        assertThat(response.talentId()).isEqualTo(talent.getId());
        assertThat(response.talentTitle()).isEqualTo("로고 디자인");
        assertThat(response.buyerId()).isEqualTo(buyer.getId());
        assertThat(response.sellerId()).isEqualTo(seller.getId());
        assertThat(response.opponentId()).isEqualTo(seller.getId());
        assertThat(response.opponentNickname()).isEqualTo("판매자");
        assertThat(response.lastMessage()).isEqualTo(lastMessage.getContent());
        assertThat(response.roomType()).isEqualTo(ChatRoomType.TRANSACTION);
    }

    @Test
    @DisplayName("findMyChatRooms - 커서 이후 채팅방을 lastMessageAt desc, roomId desc 순으로 조회한다")
    void findMyChatRooms_withCursor() {
        User buyer = saveUser("cursor-buyer@test.com", "커서구매자");
        User seller = saveUser("cursor-seller@test.com", "커서판매자");

        Category category = categoryRepository.save(Category.create("개발", 2));
        Talent talent = talentRepository.save(Talent.create(
                seller.getId(),
                category,
                "API 개발",
                "API 개발을 도와드립니다.",
                5,
                200
        ));

        LocalDateTime sameSortAt = LocalDateTime.of(2026, 6, 25, 12, 0);
        LocalDateTime olderSortAt = LocalDateTime.of(2026, 6, 25, 11, 0);

        ChatRoom sameTimeLowerRoom = saveTransactionRoom(
                talent.getId(), buyer.getId(), seller.getId(), 201L, sameSortAt
        );

        ChatRoom cursorRoom = saveTransactionRoom(
                talent.getId(), buyer.getId(), seller.getId(), 202L, sameSortAt
        );

        ChatRoom sameTimeHigherRoom = saveTransactionRoom(
                talent.getId(), buyer.getId(), seller.getId(), 203L, sameSortAt
        );

        ChatRoom olderRoom = saveTransactionRoom(
                talent.getId(), buyer.getId(), seller.getId(), 204L, olderSortAt
        );

        entityManager.flush();
        entityManager.clear();

        List<ChatRoomListRes> result = chatRoomRepository.findMyChatRooms(
                buyer.getId(),
                cursorRoom.getId(),
                20
        );

        assertThat(result)
                .extracting(ChatRoomListRes::roomId)
                .containsExactly(
                        sameTimeLowerRoom.getId(),
                        olderRoom.getId()
                );

        assertThat(result)
                .extracting(ChatRoomListRes::roomId)
                .doesNotContain(
                        cursorRoom.getId(),
                        sameTimeHigherRoom.getId()
                );
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.builder()
                .email(email)
                .password("password")
                .nickname(nickname)
                .profileImageUrl(null)
                .introduction("테스트 소개")
                .trustScore(BigDecimal.ZERO)
                .build());
    }

    private ChatRoom saveTransactionRoom(
            Long talentId,
            Long buyerId,
            Long sellerId,
            Long tradeId,
            LocalDateTime lastMessageAt
    ) {
        ChatRoom chatRoom = ChatRoom.createForTransaction(
                talentId,
                buyerId,
                sellerId,
                tradeId
        );

        ReflectionTestUtils.setField(chatRoom, "lastMessageAt", lastMessageAt);

        return chatRoomRepository.save(chatRoom);
    }
}