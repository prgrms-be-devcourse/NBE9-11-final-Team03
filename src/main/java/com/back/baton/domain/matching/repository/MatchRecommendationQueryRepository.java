package com.back.baton.domain.matching.repository;

import com.back.baton.domain.category.entity.QCategory;
import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.talent.entity.QTalent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    public Optional<MatchRecommendationDetailRes> findMatchRecommendationDetail(
            Long categoryId,
            Long providerTalentId
    ) {
        QTalent talent = QTalent.talent;
        QCategory category = QCategory.category;
        QUser user = QUser.user;

        MatchRecommendationDetailRes result = queryFactory
                .select(Projections.constructor(MatchRecommendationDetailRes.class,
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
                        talent.viewCount,
                        user.nickname,
                        user.introduction,
                        user.profileImageUrl,
                        user.trustScore,
                        Expressions.constant(true),
                        Expressions.nullExpression(String.class)
                ))
                .from(talent)
                .join(talent.category, category)
                .join(user).on(talent.authorId.eq(user.id))
                .where(
                        talent.id.eq(providerTalentId),
                        category.id.eq(categoryId),
                        category.active.isTrue(),
                        talent.status.eq(TalentStatus.ACTIVE),
                        talent.deletedAt.isNull()
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}