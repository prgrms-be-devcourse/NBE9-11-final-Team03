package com.back.baton.domain.talent.repository;

import com.back.baton.domain.category.entity.QCategory;
import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.entity.QTalent;
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

    // 검색,필터
    @Override
    public List<TalentListRes> searchTalents(TalentSearchReq req, Long cursor, int size) {
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
                        talent.deletedAt.isNull(),       // soft delete 제외
                        cursorLt(cursor),                // 커서 (null이면 첫 페이지)
                        categoryEq(req.categoryId()),    // 동적 필터: null인 조건은 QueryDSL이 자동 무시
                        creditGoe(req.minCredit()),
                        creditLoe(req.maxCredit()),
                        ratingGoe(req.minRating()),
                        completed(req.completedOnly())
                )
                .orderBy(talent.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    // 동적 조건 BooleanExpression이 null이면 where에서 무시됨
    private BooleanExpression cursorLt(Long cursor) {
        // null이면 조건 자체를 안 건다 -> 첫 페이지는 전체에서 최신 size개
        return cursor == null ? null : QTalent.talent.id.lt(cursor);
    }

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