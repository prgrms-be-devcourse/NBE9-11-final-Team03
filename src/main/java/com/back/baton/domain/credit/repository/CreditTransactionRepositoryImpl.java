package com.back.baton.domain.credit.repository;

import com.back.baton.domain.credit.dto.request.CreditTransactionSearchReq;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import com.back.baton.domain.credit.entity.QCreditTransaction;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CreditTransactionRepositoryImpl implements CreditTransactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CreditTransactionRes> findHistory(Long userId, CreditTransactionSearchReq req, Long cursor, int size) {
        QCreditTransaction ct = QCreditTransaction.creditTransaction;

        return queryFactory
                .select(Projections.constructor(CreditTransactionRes.class,
                        ct.id,
                        ct.relatedTradeId,
                        ct.type,
                        ct.amount,
                        ct.balanceAfter,
                        ct.defaultReason,
                        ct.detailReason,
                        ct.createdAt
                ))
                .from(ct)
                .where(
                        ct.userId.eq(userId), // 본인 내역만
                        cursorLt(cursor),
                        typeEq(req.type()),
                        createdGoe(req.from()),
                        createdLoe(req.to())
                )
                .orderBy(ct.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    // 동적 조건 BooleanExpression이 null이면 where에서 무시됨
    private BooleanExpression cursorLt(Long cursor) {
        return cursor == null ? null : QCreditTransaction.creditTransaction.id.lt(cursor);
    }

    private BooleanExpression typeEq(CreditTransactionType type) {
        return type == null ? null : QCreditTransaction.creditTransaction.type.eq(type);
    }

    private BooleanExpression createdGoe(LocalDateTime from) {
        return from == null ? null : QCreditTransaction.creditTransaction.createdAt.goe(from);
    }

    private BooleanExpression createdLoe(LocalDateTime to) {
        return to == null ? null : QCreditTransaction.creditTransaction.createdAt.loe(to);
    }
}