package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.dto.request.TalentCreateReq;
import com.back.baton.domain.talent.dto.response.TalentCreateRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TalentServiceTest {

    @InjectMocks TalentService talentService;
    @Mock TalentRepository talentRepository;
    @Mock CategoryRepository categoryRepository;

    @Test
    @DisplayName("정상 입력이면 저장하고 생성된 id를 반환한다")
    void createTalent_success() {
        Long authorId = 1L;
        var request = new TalentCreateReq(10L, "스프링 코드리뷰", "내용", 2, 100);
        Category category = mock(Category.class);
        given(category.isActive()).willReturn(true);
        given(categoryRepository.findById(10L)).willReturn(Optional.of(category));
        ReflectionTestUtils.setField(talentService, "maxTalentCountPerUser", 10);
        given(talentRepository.countByAuthorIdAndDeletedAtIsNull(authorId)).willReturn(0);

        Talent saved = Talent.create(authorId, mock(Category.class), "스프링 코드리뷰", "내용", 2, 100);
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(talentRepository.save(any(Talent.class))).willReturn(saved);

        TalentCreateRes res = talentService.createTalent(authorId, request);

        assertThat(res.talentId()).isEqualTo(100L);
        then(talentRepository).should().save(any(Talent.class));
    }

    @Test
    @DisplayName("등록 개수가 상한에 도달하면 TALENT_REGISTRATION_LIMIT_EXCEEDED, 저장하지 않는다")
    void createTalent_limitExceeded() {
        var request = new TalentCreateReq(10L, "제목", "내용", 2, 100);
        Category category = mock(Category.class);
        given(category.isActive()).willReturn(true);
        given(categoryRepository.findById(10L)).willReturn(Optional.of(category));
        ReflectionTestUtils.setField(talentService, "maxTalentCountPerUser", 10);
        given(talentRepository.countByAuthorIdAndDeletedAtIsNull(1L)).willReturn(10);

        assertThatThrownBy(() -> talentService.createTalent(1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.TALENT_REGISTRATION_LIMIT_EXCEEDED));
        then(talentRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리면 예외를 던지고 저장하지 않는다")
    void createTalent_categoryNotFound() {
        var request = new TalentCreateReq(999L, "제목", "내용", 2, 100);
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> talentService.createTalent(1L, request))
                .isInstanceOf(CustomException.class);
        then(talentRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("비활성 카테고리면 예외를 던지고 저장하지 않는다")
    void createTalent_categoryInactive() {
        var request = new TalentCreateReq(10L, "제목", "내용", 2, 100);
        Category inactive = mock(Category.class);
        given(categoryRepository.findById(10L)).willReturn(Optional.of(inactive));
        given(inactive.isActive()).willReturn(false);

        assertThatThrownBy(() -> talentService.createTalent(1L, request))
                .isInstanceOf(CustomException.class);
        then(talentRepository).should(never()).save(any());
    }
}