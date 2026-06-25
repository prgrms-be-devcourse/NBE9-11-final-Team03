package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.dto.request.TalentCreateReq;
import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.request.TalentUpdateReq;
import com.back.baton.domain.talent.dto.response.TalentCreateRes;
import com.back.baton.domain.talent.dto.response.TalentDetailRes;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.dto.response.TalentUpdateRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentSortType;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.global.response.code.TalentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentService {

    private final TalentRepository talentRepository;
    private final CategoryRepository categoryRepository;
    private final TradeRepository tradeRepository;
    private static final int MAX_PAGE_SIZE = 100;

    @Value("${talent.max-count-per-user:3}")
    private int maxTalentCountPerUser;

    // 재능 등록
    @Transactional
    public TalentCreateRes createTalent(Long authorId, TalentCreateReq request) {

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(TalentErrorCode.CATEGORY_NOT_FOUND));

        if (!category.isActive()) { // 비활성 카테고리 등록 차단
            throw new CustomException(TalentErrorCode.CATEGORY_INACTIVE);
        }

        // 1인당 등록 개수 제한 (삭제되지 않은 재능 기준)
        int currentCount = talentRepository.countByAuthorIdAndDeletedAtIsNull(authorId);
        if (currentCount >= maxTalentCountPerUser) {
            throw new CustomException(TalentErrorCode.TALENT_REGISTRATION_LIMIT_EXCEEDED);
        }

        Talent talent = Talent.create(authorId, category,
                request.title(), request.content(),
                request.estimatedHours(), request.creditPrice());

        return TalentCreateRes.from(talentRepository.save(talent));
    }

    //재능 수정
    @Transactional
    public TalentUpdateRes updateTalent(Long talentId, Long authorId, TalentUpdateReq request) {

        // 대상 재능 확인
        Talent talent = talentRepository.getActiveTalentOrThrow(talentId);

        // 소유권 확인, 다른 사람 글이면 차단
        if(!talent.getAuthorId().equals(authorId)) {
            throw new CustomException(TalentErrorCode.TALENT_FORBIDDEN);
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(TalentErrorCode.CATEGORY_NOT_FOUND));
        if (!category.isActive()) {
            throw new CustomException(TalentErrorCode.CATEGORY_INACTIVE);
        }

        talent.update(category, request.title(), request.content(), request.estimatedHours(), request.creditPrice());
        // Dirty Checking: save() 없이 커밋 시 UPDATE
        // TODO: 캐시 도입(TALENT 카테고리/상세 캐싱) 시 @CacheEvict로 무효화 추가
        return TalentUpdateRes.from(talent);
    }

    @Transactional
    public void deleteTalent(Long talentId, Long authorId) {
        Talent talent = talentRepository.getActiveTalentOrThrow(talentId);

        //소유권 확인
        if(!talent.getAuthorId().equals(authorId)) {
            throw new CustomException(TalentErrorCode.TALENT_FORBIDDEN);
        }

        List<TradeStatus> blockStatuses = List.of(TradeStatus.IN_PROGRESS, TradeStatus.UNDER_REVIEW);
        boolean hasUnfinishedTrade = tradeRepository.existsByTalentIdAndStatusIn(talentId, blockStatuses);

        if (hasUnfinishedTrade) {
            throw new CustomException(TalentErrorCode.TALENT_CANNOT_DELETE);
        }
        // Dirty Checking: save() 없이 커밋 시 UPDATE
        talent.softDelete();
        // TODO: 캐시 도입(TALENT 카테고리/상세 캐싱) 시 @CacheEvict로 무효화 추가
    }

    // 커서 페이징 (공통 CursorPageRes 사용)
    public CursorPageRes<TalentListRes> getTalentList(Long cursor, int size, TalentSortType sort) {
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        TalentSortType sortType = (sort != null) ? sort : TalentSortType.LATEST; // null 방어
        List<TalentListRes> rows = talentRepository.findTalentList(cursor, pageSize, sortType);

        return CursorPageRes.from(rows, pageSize, TalentListRes::talentId);
    }


    // 재능 상세 조회 + 조회수 증가
    @Transactional
    public TalentDetailRes getTalentDetail(Long talentId) {
        List<Object[]> rows = talentRepository.findDetailById(talentId);
        if (rows.isEmpty()) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND); // 없음/삭제
        }

        Object[] row = rows.get(0);
        Talent talent = (Talent) row[0];
        User author = (User) row[1];

        TalentDetailRes response = TalentDetailRes.from(talent, author);
        talentRepository.increaseViewCount(talentId);
        return response;
    }

    // 검색,필터 (공통 CursorPageRes 사용)
    public CursorPageRes<TalentListRes> searchTalents(TalentSearchReq req, Long cursor, int size, TalentSortType sort) {
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        TalentSortType sortType = (sort != null) ? sort : TalentSortType.LATEST;
        List<TalentListRes> rows = talentRepository.searchTalents(req, cursor, pageSize, sortType);
        return CursorPageRes.from(rows, pageSize, TalentListRes::talentId);
    }

}