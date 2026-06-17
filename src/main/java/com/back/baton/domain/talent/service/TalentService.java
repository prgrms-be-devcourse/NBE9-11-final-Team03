package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.dto.request.TalentCreateReq;
import com.back.baton.domain.talent.dto.request.TalentUpdateReq;
import com.back.baton.domain.talent.dto.response.TalentCreateRes;
import com.back.baton.domain.talent.dto.response.TalentUpdateRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentService {

    private final TalentRepository talentRepository;
    private final CategoryRepository categoryRepository;

    // 재능 등록
    @Transactional
    public TalentCreateRes createTalent(Long authorId, TalentCreateReq request) {

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(TalentErrorCode.CATEGORY_NOT_FOUND));

        if (!category.isActive()) { // 비활성 카테고리 등록 차단
            throw new CustomException(TalentErrorCode.CATEGORY_INACTIVE);
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
        Talent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));

        // 삭제 된 글은 없는 것으로 취급
        if(talent.isDeleted()) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND);
        }

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
        Talent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));

        // 이미 삭제된 글은 없는 것 (소유권보다 먼저)
        if(talent.isDeleted()) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND);
        }

        //소유권 확인
        if(!talent.getAuthorId().equals(authorId)) {
            throw new CustomException(TalentErrorCode.TALENT_FORBIDDEN);
        }

        // TODO: 진행 중 거래 match 도메인 생성 되면 삭제 차단
        talent.softDelete();
        // Dirty Checking: save() 없이 커밋 시 UPDATE

        // TODO: 캐시 도입(TALENT 카테고리/상세 캐싱) 시 @CacheEvict로 무효화 추가
    }
}