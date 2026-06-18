package com.back.baton.domain.matching.repository;

import com.back.baton.domain.category.entity.QCategory;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.talent.entity.QTalent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class MatchRecommendationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<MatchRecommendationRes> findMatchRecommendations(
            Long categoryId,
            Long requesterId,
            List<Long> excludedTalentIds
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
                        talent.completeCount
                ))
                .from(talent)
                .join(talent.category, category)
                .where(
                        category.id.eq(categoryId),
                        talent.authorId.ne(requesterId),
                        talent.status.eq(TalentStatus.ACTIVE),
                        talent.deletedAt.isNull(),
                        notInExcludedTalentIds(excludedTalentIds)
                )
                .orderBy(
                        talent.avgRating.desc(),
                        talent.completeCount.desc(),
                        talent.viewCount.desc(),
                        talent.id.desc()
                )
                .fetch();
    }

    private BooleanExpression notInExcludedTalentIds(List<Long> excludedTalentIds) {
        if (excludedTalentIds == null) {
            return null;
        }

        List<Long> nonNullExcludedTalentIds = excludedTalentIds.stream()
                .filter(Objects::nonNull)
                .toList();

        return nonNullExcludedTalentIds.isEmpty() ? null : QTalent.talent.id.notIn(nonNullExcludedTalentIds);
    }
}