package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.entity.ChatMessage;
import com.back.baton.domain.chat.entity.QChatMessage;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessage> findMessages(Long roomId, Long cursor, int size) {
        QChatMessage chatMessage = QChatMessage.chatMessage;

        return queryFactory
                .selectFrom(chatMessage)
                .where(
                        chatMessage.chatRoom.id.eq(roomId),
                        chatMessage.deletedAt.isNull(),
                        cursorLt(chatMessage, cursor)
                )
                .orderBy(chatMessage.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    private BooleanExpression cursorLt(QChatMessage chatMessage, Long cursor) {
        if (cursor == null) {
            return null;
        }

        return chatMessage.id.lt(cursor);
    }
}