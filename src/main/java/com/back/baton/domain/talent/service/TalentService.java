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
import com.back.baton.global.response.code.ErrorCode;
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
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.isActive()) { // 비활성 카테고리 등록 차단
            throw new CustomException(ErrorCode.CATEGORY_INACTIVE);
        }

        Talent talent = Talent.create(authorId, category,
                request.title(), request.content(),
                request.estimatedHours(), request.creditPrice());

        return TalentCreateRes.from(talentRepository.save(talent));
    }

    //재능 수정
    @Transactional
    public TalentUpdateRes updateTalent(Long talentId, Long authorId, TalentUpdateReq request) {
        Talent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new CustomException(ErrorCode.TALENT_NOT_FOUND));

        if(talent.isDeleted()) {
            throw new CustomException(ErrorCode.TALENT_NOT_FOUND);
        }
        if(!talent.getAuthorId().equals(authorId)) {
            throw new CustomException(ErrorCode.TALENT_NOT_FOUND);
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        talent.update(category, request.title(), request.content(), request.estimatedHours(), request.creditPrice());
        // TODO: @CacheEvict — 상세 캐시(TALENT-06),카테고리 캐시(TALENT-10) 들어오면 무효화 추가
        return TalentUpdateRes.from(talent);
    }
}