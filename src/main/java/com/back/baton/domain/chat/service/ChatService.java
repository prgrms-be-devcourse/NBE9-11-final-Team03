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
            Long buyerId,
            Long sellerId
    ) {
        Talent talent = getTalent(talentId);

        validateSelfChat(buyerId, sellerId);
        validateSellerOwnsTalent(sellerId, talent);

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

    public List<ChatMessageRes> getMessages(Long roomId, Long userId) {
        getChatRoom(roomId, userId);

        return chatMessageRepository.findMessages(roomId)
                .stream()
                .map(ChatMessageRes::from)
                .toList();
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

    private void validateSellerOwnsTalent(Long sellerId, Talent talent) {
        if (!Objects.equals(talent.getAuthorId(), sellerId)) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }

    private void validateParticipant(ChatRoom chatRoom, Long userId) {
        if (!chatRoom.isParticipant(userId)) {
            throw new CustomException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}