package com.back.baton.domain.matching.repository;

import com.back.baton.domain.category.entity.QCategory;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.talent.entity.QTalent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MatchRecommendationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<MatchRecommendationRes> findMatchRecommendations(
            Long categoryId,
            Long requesterId
    ) {
        QTalent talent = QTalent.talent;
        QCategory category = QCategory.category;

        return queryFactory
                .select(Projections.constructor(MatchRecommendationRes.class,
                        talent.id,
                        talent.authorId,
                        category.id,
                        category.name,
                        talent.title,
                        talent.content,
                        talent.creditPrice,
                        talent.estimatedHours,
                        talent.avgRating,
                        talent.completeCount,
                        Expressions.constant(true),
                        Expressions.nullExpression(String.class)
                ))
                .from(talent)
                .join(talent.category, category)
                .where(
                        category.id.eq(categoryId),
                        category.active.isTrue(),
                        talent.authorId.ne(requesterId),
                        talent.status.eq(TalentStatus.ACTIVE),
                        talent.deletedAt.isNull()
                )
                .orderBy(
                        talent.avgRating.desc(),
                        talent.completeCount.desc(),
                        talent.viewCount.desc(),
                        talent.id.desc()
                )
                .limit(3)
                .fetch();
    }
}