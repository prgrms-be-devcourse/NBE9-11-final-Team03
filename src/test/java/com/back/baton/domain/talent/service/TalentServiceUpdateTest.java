package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.dto.request.TalentUpdateReq;
import com.back.baton.domain.talent.dto.response.TalentUpdateRes;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TalentServiceUpdateTest {

    @InjectMocks TalentService talentService;
    @Mock TalentRepository talentRepository;
    @Mock CategoryRepository categoryRepository;

    @Test
    @DisplayName("정상 수정이면 필드가 변경되고 응답을 반환한다")
    void updateTalent_success() {
        // given: 작성자 1L의 재능 활성 카테고리 9L로 변경 요청
        Long authorId = 1L;
        Category newCat = mock(Category.class);
        given(newCat.getId()).willReturn(9L);
        given(newCat.isActive()).willReturn(true);

        Talent talent = Talent.create(authorId, mock(Category.class), "기존", "기존내용", 2, 100);
        given(talentRepository.findById(10L)).willReturn(Optional.of(talent));
        given(categoryRepository.findById(9L)).willReturn(Optional.of(newCat));

        // when
        var request = new TalentUpdateReq(9L, "수정", "수정내용", 3, 200);
        TalentUpdateRes res = talentService.updateTalent(10L, authorId, request);

        // then
        assertThat(talent.getTitle()).isEqualTo("수정");
        assertThat(talent.getCreditPrice()).isEqualTo(200);
        assertThat(talent.getCategory()).isEqualTo(newCat);
        assertThat(res.title()).isEqualTo("수정");
    }

    @Test
    @DisplayName("재능이 없으면 TALENT_NOT_FOUND")
    void updateTalent_notFound() {
        // given: 조회 결과 없음
        given(talentRepository.findById(99L)).willReturn(Optional.empty());
        var request = new TalentUpdateReq(1L, "t", "c", 1, 0);

        // when & then: 존재 검증에서 막힘
        assertThatThrownBy(() -> talentService.updateTalent(99L, 1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.TALENT_NOT_FOUND));
    }

    @Test
    @DisplayName("본인 글이 아니면 TALENT_FORBIDDEN, 카테고리 조회 안 함")
    void updateTalent_forbidden() {
        // given: 작성자는 1L
        Talent talent = Talent.create(1L, mock(Category.class), "t", "c", 1, 0);
        given(talentRepository.findById(10L)).willReturn(Optional.of(talent));
        var request = new TalentUpdateReq(1L, "t", "c", 1, 0);

        // when & then: 요청자 2L(남의 글) -> 소유권에서 막히고 카테고리 조회까지 가지 않음
        assertThatThrownBy(() -> talentService.updateTalent(10L, 2L, request)) // 작성자1 요청자2
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.TALENT_FORBIDDEN));
        then(categoryRepository).shouldHaveNoInteractions(); // 소유권에서 끊겨 카테고리 조회 0회
    }

    @Test
    @DisplayName("카테고리가 없으면 CATEGORY_NOT_FOUND")
    void updateTalent_categoryNotFound() {
        // given: 본인 글이지만 변경할 카테고리 9L이 존재하지 않음
        Talent talent = Talent.create(1L, mock(Category.class), "t", "c", 1, 0);
        given(talentRepository.findById(10L)).willReturn(Optional.of(talent));
        given(categoryRepository.findById(9L)).willReturn(Optional.empty());
        var request = new TalentUpdateReq(9L, "t", "c", 1, 0);

        // when & then: 카테고리 존재 검증에서 막힘
        assertThatThrownBy(() -> talentService.updateTalent(10L, 1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("비활성 카테고리면 CATEGORY_INACTIVE")
    void updateTalent_categoryInactive() {
        // given: 변경할 카테고리 9L이 비활성 상태
        Talent talent = Talent.create(1L, mock(Category.class), "t", "c", 1, 0);
        Category inactive = mock(Category.class);
        given(inactive.isActive()).willReturn(false);
        given(talentRepository.findById(10L)).willReturn(Optional.of(talent));
        given(categoryRepository.findById(9L)).willReturn(Optional.of(inactive));
        var request = new TalentUpdateReq(9L, "t", "c", 1, 0);

        // when & then: 카테고리 활성 검증에서 막힘
        assertThatThrownBy(() -> talentService.updateTalent(10L, 1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.CATEGORY_INACTIVE));
    }
}