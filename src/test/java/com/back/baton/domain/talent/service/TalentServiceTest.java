package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.dto.request.TalentCreateReq;
import com.back.baton.domain.talent.dto.response.TalentCreateRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
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
        given(categoryRepository.findById(10L)).willReturn(Optional.of(mock(Category.class)));

        Talent saved = Talent.create(authorId, mock(Category.class), "스프링 코드리뷰", "내용", 2, 100);
        ReflectionTestUtils.setField(saved, "id", 100L); // save 후 id 채워진 상태 모사
        given(talentRepository.save(any(Talent.class))).willReturn(saved);

        TalentCreateRes res = talentService.createTalent(authorId, request);

        assertThat(res.talentId()).isEqualTo(100L);
        then(talentRepository).should().save(any(Talent.class));
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
}