package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.talent.dto.response.TalentDetailRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TalentServiceDetailTest {

    @InjectMocks TalentService talentService;
    @Mock TalentRepository talentRepository;

    @Test
    @DisplayName("상세 조회 성공: 증가 전 viewCount 반환 + 조회수 1회 증가")
    void getTalentDetail_success() {
        Talent talent = Talent.create(7L, mock(Category.class), "웹페이지 개발", "내용", 3, 500);
        ReflectionTestUtils.setField(talent, "id", 1L);
        ReflectionTestUtils.setField(talent, "viewCount", 10);

        User author = User.builder()
                .email("a@test.com").password("p").nickname("박재현")
                .profileImageUrl("https://img/7.png").introduction("소개입니다")
                .trustScore(new BigDecimal("36.50")).build();
        ReflectionTestUtils.setField(author, "id", 7L);

        given(talentRepository.findDetailById(1L))
                .willReturn(List.<Object[]>of(new Object[]{talent, author}));

        TalentDetailRes res = talentService.getTalentDetailWithViewCount(1L);

        assertThat(res.viewCount()).isEqualTo(10);            // 증가 전 값
        assertThat(res.author().nickname()).isEqualTo("박재현");
        then(talentRepository).should(times(1)).increaseViewCount(1L);
    }

    @Test
    @DisplayName("상세 조회 실패(없음/삭제): 404 + 조회수 증가 미호출")
    void getTalentDetail_notFound() {
        given(talentRepository.findDetailById(99L)).willReturn(List.of());
        // increaseViewCount는 호출 안 됨 -> stub 자체를 안

        assertThatThrownBy(() -> talentService.getTalentDetailWithViewCount(99L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.TALENT_NOT_FOUND));

        then(talentRepository).should(never()).increaseViewCount(anyLong());
    }

    @Test
    @DisplayName("increaseView=false면 조회수를 증가시키지 않는다 (채팅/거래/크레딧 등 정보 조회용)")
    void getTalentDetail_withoutIncreaseView() {
        Talent talent = Talent.create(7L, mock(Category.class), "웹페이지 개발", "내용", 3, 500);
        ReflectionTestUtils.setField(talent, "id", 1L);
        ReflectionTestUtils.setField(talent, "viewCount", 10);

        User author = User.builder()
                .email("a@test.com").password("p").nickname("박재현")
                .profileImageUrl("https://img/7.png").introduction("소개입니다")
                .trustScore(new BigDecimal("36.50")).build();
        ReflectionTestUtils.setField(author, "id", 7L);

        given(talentRepository.findDetailById(1L))
                .willReturn(List.<Object[]>of(new Object[]{talent, author}));

        TalentDetailRes res = talentService.getTalentDetail(1L);

        assertThat(res.viewCount()).isEqualTo(10);
        then(talentRepository).should(never()).increaseViewCount(anyLong());
    }
}