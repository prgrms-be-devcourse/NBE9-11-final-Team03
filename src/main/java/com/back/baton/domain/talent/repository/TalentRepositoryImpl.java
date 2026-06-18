package com.back.baton.domain.talent.repository;

import com.back.baton.domain.category.entity.QCategory;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.entity.QTalent;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TalentRepositoryImpl implements TalentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TalentListRes> findTalentList(Long cursor, int size) {
        QTalent talent = QTalent.talent;
        QCategory category = QCategory.category;

        return queryFactory
                .select(Projections.constructor(TalentListRes.class,
                        talent.id,
                        category.name,
                        talent.title,
                        talent.creditPrice,
                        talent.estimatedHours,
                        talent.avgRating,
                        talent.completeCount,
                        talent.viewCount,
                        talent.createdAt
                ))
                .from(talent)
                .join(talent.category, category)
                .where(
                        talent.deletedAt.isNull(),   // 소프트 삭제 제외
                        cursorLt(cursor)             // 커서 (첫 페이지면 null -> 무시)
                )
                .orderBy(talent.id.desc())           // 최신순 (id = auto increment)
                .limit(size + 1L)                    // hasNext 판단용 +1
                .fetch();
    }

    private BooleanExpression cursorLt(Long cursor) {
        // null이면 조건 자체를 안 건다 -> 첫 페이지는 전체에서 최신 size개
        return cursor == null ? null : QTalent.talent.id.lt(cursor);
    }
}