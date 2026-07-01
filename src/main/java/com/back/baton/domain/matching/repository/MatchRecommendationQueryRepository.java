package com.back.baton.domain.matching.repository;

import com.back.baton.domain.category.entity.QCategory;
import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.profile.entity.QProfile;
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
            List<Long> requesterWantCategoryIds,
            Long requesterId
    ) {
        QTalent talent = QTalent.talent;
        QTalent requesterTalent = new QTalent("requesterTalent");
        QCategory category = QCategory.category;
        QCategory requesterTalentCategory = new QCategory("requesterTalentCategory");
        QProfile profile = QProfile.profile;
        QCategory providerWantCategory = new QCategory("providerWantCategory");

        return queryFactory
                .select(Projections.constructor(MatchRecommendationRes.class,
                        talent.id,
                        requesterTalent.id.min(),
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
                .join(requesterTalent).on(requesterTalent.authorId.eq(requesterId))
                .join(requesterTalent.category, requesterTalentCategory)
                .join(profile).on(profile.user.id.eq(talent.authorId))
                .join(profile.wantTalentCategories, providerWantCategory)
                .where(
                        providerWantCategory.id.eq(requesterTalentCategory.id),
                        category.id.in(requesterWantCategoryIds),
                        category.active.isTrue(),
                        requesterTalentCategory.active.isTrue(),
                        providerWantCategory.active.isTrue(),
                        talent.authorId.ne(requesterId),
                        requesterTalent.status.eq(TalentStatus.ACTIVE),
                        requesterTalent.deletedAt.isNull(),
                        talent.status.eq(TalentStatus.ACTIVE),
                        talent.deletedAt.isNull()
                )
                .groupBy(
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
                        talent.viewCount
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
            Long requesterTalentCategoryId,
            List<Long> requesterWantCategoryIds,
            Long providerTalentId
    ) {
        QTalent talent = QTalent.talent;
        QCategory category = QCategory.category;
        QProfile profile = QProfile.profile;
        QCategory providerWantCategory = new QCategory("providerWantCategory");
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
                .join(profile).on(profile.user.id.eq(talent.authorId))
                .join(profile.wantTalentCategories, providerWantCategory)
                .join(user).on(talent.authorId.eq(user.id))
                .where(
                        talent.id.eq(providerTalentId),
                        providerWantCategory.id.eq(requesterTalentCategoryId),
                        category.id.in(requesterWantCategoryIds),
                        category.active.isTrue(),
                        providerWantCategory.active.isTrue(),
                        talent.status.eq(TalentStatus.ACTIVE),
                        talent.deletedAt.isNull()
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
