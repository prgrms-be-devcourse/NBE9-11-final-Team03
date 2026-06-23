package com.back.baton.domain.chat.service;

import com.back.baton.domain.chat.dto.response.ChatMessageRes;
import com.back.baton.domain.chat.dto.response.ChatRoomRes;
import com.back.baton.domain.chat.entity.ChatMessage;
import com.back.baton.domain.chat.entity.ChatRoom;
import com.back.baton.domain.chat.entity.ChatRoomType;
import com.back.baton.domain.chat.repository.ChatMessageRepository;
import com.back.baton.domain.chat.repository.ChatRoomRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.ChatErrorCode;
import com.back.baton.global.response.code.TalentErrorCode;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final TalentRepository talentRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomRes getOrCreateMatchRoom(
            Long talentId,
            Long buyerId
    ) {
        Talent talent = getTalent(talentId);
        Long sellerId = talent.getAuthorId();

        validateSelfChat(buyerId, sellerId);

        ChatRoom chatRoom = chatRoomRepository.findActiveRoom(
                        talentId,
                        buyerId,
                        sellerId,
                        ChatRoomType.MATCH
                )
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.createForMatch(
                                talentId,
                                buyerId,
                                sellerId
                        )
                ));

        return ChatRoomRes.from(chatRoom);
    }

    public ChatRoom getChatRoom(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        validateParticipant(chatRoom, userId);

        return chatRoom;
    }

    @Transactional
    public ChatRoomRes getOrCreateTransactionRoom(
            Long tradeId,
            Long talentId,
            Long buyerId,
            Long sellerId
    ) {
        validateSelfChat(buyerId, sellerId);

        ChatRoom chatRoom = chatRoomRepository.findActiveTransactionRoomByTradeId(tradeId)
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.createForTransaction(
                                talentId,
                                buyerId,
                                sellerId,
                                tradeId
                        )
                ));

        return ChatRoomRes.from(chatRoom);
    }

    public ChatRoomRes getChatRoomInfo(
            Long roomId,
            Long userId
    ) {
        ChatRoom chatRoom = getChatRoom(roomId, userId);

        return ChatRoomRes.from(chatRoom);
    }

    @Transactional
    public ChatMessageRes sendMessage(Long roomId, Long senderId, String content) {
        ChatRoom chatRoom = getChatRoom(roomId, senderId);

        ChatMessage chatMessage = ChatMessage.createTextMessage(
                chatRoom,
                senderId,
                content
        );

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        chatRoom.updateLastMessageAt();

        return ChatMessageRes.from(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageRes> getMessages(Long roomId, Long userId) {
        getChatRoom(roomId, userId);

        return chatMessageRepository.findMessages(roomId)
                .stream()
                .map(ChatMessageRes::from)
                .toList();
    }

    @Transactional
    public List<Long> markMessagesAsRead(
            Long roomId,
            Long readerId
    ) {
        getChatRoom(roomId, readerId);

        List<Long> unreadMessageIds = chatMessageRepository
                .findUnreadMessageIdsFromOtherParticipant(roomId, readerId);

        if (!unreadMessageIds.isEmpty()) {
            chatMessageRepository.markAsReadByIds(unreadMessageIds);
        }

        return unreadMessageIds;
    }

    private Talent getTalent(Long talentId) {
        return talentRepository.findById(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));
    }

    private void validateSelfChat(Long buyerId, Long sellerId) {
        if (Objects.equals(buyerId, sellerId)) {
            throw new CustomException(ChatErrorCode.SELF_CHAT_NOT_ALLOWED);
        }
    }

    private void validateParticipant(ChatRoom chatRoom, Long userId) {
        if (!chatRoom.isParticipant(userId)) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}