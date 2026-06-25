package com.back.baton.domain.chat.repository;

import com.back.baton.domain.chat.dto.response.ChatRoomListRes;
import com.back.baton.domain.chat.entity.QChatMessage;
import com.back.baton.domain.chat.entity.QChatRoom;
import com.back.baton.domain.talent.entity.QTalent;
import com.back.baton.domain.user.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatRoomListRes> findMyChatRooms(Long userId, Long cursor, int size) {
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QUser buyer = new QUser("buyer");
        QUser seller = new QUser("seller");
        QTalent talent = QTalent.talent;
        QChatMessage message = new QChatMessage("message");
        QChatMessage lastMessage = new QChatMessage("lastMessage");

        LocalDateTime cursorSortAt = findCursorSortAt(userId, cursor);
        if (cursor != null && cursorSortAt == null) {
            return List.of();
        }

        DateTimeExpression<LocalDateTime> sortAt = sortAt(chatRoom);

        return queryFactory
                .select(Projections.constructor(ChatRoomListRes.class,
                        chatRoom.id,
                        chatRoom.tradeId,
                        chatRoom.talentId,
                        talent.title,
                        chatRoom.buyerId,
                        chatRoom.sellerId,
                        new CaseBuilder()
                                .when(chatRoom.buyerId.eq(userId)).then(seller.id)
                                .otherwise(buyer.id),
                        new CaseBuilder()
                                .when(chatRoom.buyerId.eq(userId)).then(seller.nickname)
                                .otherwise(buyer.nickname),
                        new CaseBuilder()
                                .when(chatRoom.buyerId.eq(userId)).then(seller.profileImageUrl)
                                .otherwise(buyer.profileImageUrl),
                        JPAExpressions
                                .select(message.content)
                                .from(message)
                                .where(message.id.eq(
                                        JPAExpressions
                                                .select(lastMessage.id.max())
                                                .from(lastMessage)
                                                .where(
                                                        lastMessage.chatRoom.id.eq(chatRoom.id),
                                                        lastMessage.deletedAt.isNull()
                                                )
                                )),
                        chatRoom.lastMessageAt,
                        chatRoom.status,
                        chatRoom.createdAt
                ))
                .from(chatRoom)
                .join(buyer).on(buyer.id.eq(chatRoom.buyerId))
                .join(seller).on(seller.id.eq(chatRoom.sellerId))
                .leftJoin(talent).on(talent.id.eq(chatRoom.talentId))
                .where(
                        chatRoom.buyerId.eq(userId).or(chatRoom.sellerId.eq(userId)),
                        cursorLt(sortAt, chatRoom, cursor, cursorSortAt)
                )
                .orderBy(sortAt.desc(), chatRoom.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    private LocalDateTime findCursorSortAt(Long userId, Long cursor) {
        if (cursor == null) {
            return null;
        }

        QChatRoom cursorRoom = new QChatRoom("cursorRoom");

        Tuple row = queryFactory
                .select(cursorRoom.lastMessageAt, cursorRoom.createdAt)
                .from(cursorRoom)
                .where(
                        cursorRoom.id.eq(cursor),
                        cursorRoom.buyerId.eq(userId).or(cursorRoom.sellerId.eq(userId))
                )
                .fetchOne();

        if (row == null) {
            return null;
        }

        LocalDateTime lastMessageAt = row.get(cursorRoom.lastMessageAt);
        LocalDateTime createdAt = row.get(cursorRoom.createdAt);

        return lastMessageAt != null ? lastMessageAt : createdAt;
    }

    private BooleanExpression cursorLt(
            DateTimeExpression<LocalDateTime> sortAt,
            QChatRoom chatRoom,
            Long cursor,
            LocalDateTime cursorSortAt
    ) {
        if (cursor == null) {
            return null;
        }

        return sortAt.lt(cursorSortAt)
                .or(sortAt.eq(cursorSortAt).and(chatRoom.id.lt(cursor)));
    }

    private DateTimeExpression<LocalDateTime> sortAt(QChatRoom chatRoom) {
        return Expressions.dateTimeTemplate(
                LocalDateTime.class,
                "coalesce({0}, {1})",
                chatRoom.lastMessageAt,
                chatRoom.createdAt
        );
    }
}