package com.back.baton.domain.trade.repository;

import com.back.baton.domain.trade.dto.response.TradeListRes;
import com.back.baton.domain.trade.entity.QTrade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TradeListRes> findMyTrades(Long userId, TradeStatus status, Long cursor, int size) {
        QTrade trade = QTrade.trade;

        return queryFactory
                .select(Projections.constructor(TradeListRes.class,
                        trade.id,
                        trade.talentId,
                        trade.buyerId,
                        trade.sellerId,
                        trade.creditPrice,
                        trade.tradeType,
                        trade.status,
                        trade.createdAt,
                        trade.updatedAt
                ))
                .from(trade)
                .where(
                        trade.buyerId.eq(userId).or(trade.sellerId.eq(userId)),
                        statusEq(status),
                        cursorLt(cursor)
                )
                .orderBy(trade.id.desc()) // 거래ID 기준 내림차순 정렬
                .limit(size + 1L)
                .fetch();
    }

    private BooleanExpression statusEq(TradeStatus status) {
        return status == null ? null : QTrade.trade.status.eq(status);
    }

    private BooleanExpression cursorLt(Long cursor) {
        return cursor == null ? null : QTrade.trade.id.lt(cursor);
    }
}