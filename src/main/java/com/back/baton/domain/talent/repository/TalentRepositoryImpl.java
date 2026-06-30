package com.back.baton.domain.talent.repository;

import com.back.baton.domain.category.entity.QCategory;
import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.entity.QTalent;
import com.back.baton.domain.talent.entity.TalentSortType;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class TalentRepositoryImpl implements TalentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TalentListRes> findTalentList(Long cursor, int size, TalentSortType sort) {
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
                        talent.status.eq(TalentStatus.ACTIVE),
                        cursorCondition(cursor, sort, talent)  // 커서 (첫 페이지면 null -> 무시)
                )
                .orderBy(orderSpecifiers(sort, talent))  // 정렬 키별 분기
                .limit(size + 1L)                    // hasNext 판단용 +1
                .fetch();
    }

    // 검색,필터
    @Override
    public List<TalentListRes> searchTalents(TalentSearchReq req, Long cursor, int size, TalentSortType sort) {
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
                        talent.deletedAt.isNull(),
                        talent.status.eq(TalentStatus.ACTIVE),// soft delete 제외
                        cursorCondition(cursor, sort, talent),  // 커서 (null이면 첫 페이지)
                        categoryEq(req.categoryId()),    // 동적 필터: null인 조건은 QueryDSL이 자동 무시
                        creditGoe(req.minCredit()),
                        creditLoe(req.maxCredit()),
                        ratingGoe(req.minRating()),
                        completed(req.completedOnly())
                )
                .orderBy(orderSpecifiers(sort, talent))
                .limit(size + 1L)
                .fetch();
    }

    // 커서 조건: 정렬 키별 keyset 비교
    // LATEST는 id 단일 커서, RATING/POPULAR는 커서 id로 정렬값을 룩업한 뒤 복합 비교
    private BooleanExpression cursorCondition(Long cursor, TalentSortType sort, QTalent talent) {
        if (cursor == null) {
            return null; // 첫 페이지는 전체에서 정렬 기준 상위 size개
        }
        return switch (sort) {
            case LATEST -> talent.id.lt(cursor);
            case RATING -> {
                // 커서 id의 평점을 한 번 룩업해 복합 비교 기준값으로 사용
                BigDecimal anchor = queryFactory
                        .select(talent.avgRating).from(talent)
                        .where(talent.id.eq(cursor))
                        .fetchOne();
                // anchor가 없으면(삭제/잘못된 커서) 첫 페이지처럼 처리
                yield anchor == null ? null
                        : talent.avgRating.lt(anchor)
                        .or(talent.avgRating.eq(anchor).and(talent.id.lt(cursor)));
            }
            case POPULAR -> {
                Integer anchor = queryFactory
                        .select(talent.completeCount).from(talent)
                        .where(talent.id.eq(cursor))
                        .fetchOne();
                yield anchor == null ? null
                        : talent.completeCount.lt(anchor)
                        .or(talent.completeCount.eq(anchor).and(talent.id.lt(cursor)));
            }
        };
    }

    // 정렬 키 (tie-breaker로 항상 id DESC)
    private OrderSpecifier<?>[] orderSpecifiers(TalentSortType sort, QTalent talent) {
        return switch (sort) {
            case LATEST  -> new OrderSpecifier<?>[]{ talent.id.desc() };          // 최신순 (id = auto increment)
            case RATING  -> new OrderSpecifier<?>[]{ talent.avgRating.desc(), talent.id.desc() };
            case POPULAR -> new OrderSpecifier<?>[]{ talent.completeCount.desc(), talent.id.desc() };
        };
    }

    // 동적 조건 BooleanExpression이 null이면 where에서 무시됨
    private BooleanExpression categoryEq(Long categoryId) {
        return categoryId == null ? null : QTalent.talent.category.id.eq(categoryId);
    }

    private BooleanExpression creditGoe(Integer minCredit) {
        return minCredit == null ? null : QTalent.talent.creditPrice.goe(minCredit);
    }

    private BooleanExpression creditLoe(Integer maxCredit) {
        return maxCredit == null ? null : QTalent.talent.creditPrice.loe(maxCredit);
    }

    private BooleanExpression ratingGoe(BigDecimal minRating) {
        return minRating == null ? null : QTalent.talent.avgRating.goe(minRating);
    }

    // 3-state: null=무시, false=무시, true일 때만 완료 1건 이상 필터
    private BooleanExpression completed(Boolean completedOnly) {
        return (completedOnly == null || !completedOnly)
                ? null
                : QTalent.talent.completeCount.goe(1);
    }
}