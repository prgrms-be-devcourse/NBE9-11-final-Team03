package com.back.baton.domain.talent.service;

import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.entity.TalentSortType;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.response.CursorPageRes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TalentServiceListTest {

    @InjectMocks TalentService talentService;
    @Mock TalentRepository talentRepository;

    @Test
    @DisplayName("size+1개가 조회되면 hasNext=true, size개로 자르고 마지막 id를 nextCursor로 준다")
    void getTalentList_hasNext() {
        // given: size=2인데 리포지토리가 3개(size+1) 반환 -> 다음 페이지 있음
        int size = 2;
        List<TalentListRes> rows = List.of(row(5L), row(4L), row(3L));
        given(talentRepository.findTalentList(null, size, TalentSortType.LATEST)).willReturn(rows);

        // when
        CursorPageRes<TalentListRes> result = talentService.getTalentList(null, size, TalentSortType.LATEST);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).hasSize(2);                       // +1 잘라냄
        assertThat(result.content()).extracting(TalentListRes::talentId)
                .containsExactly(5L, 4L);
        assertThat(result.nextCursor()).isEqualTo(4L);                 // 마지막 항목 id
    }

    @Test
    @DisplayName("size 이하로 조회되면 hasNext=false, nextCursor=null")
    void getTalentList_lastPage() {
        // given: size=2인데 2개만 옴 -> 마지막 페이지
        int size = 2;
        given(talentRepository.findTalentList(null, size, TalentSortType.LATEST))
                .willReturn(List.of(row(2L), row(1L)));

        // when
        CursorPageRes<TalentListRes> result = talentService.getTalentList(null, size, TalentSortType.LATEST);

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("size가 상한(100)을 넘으면 100으로 잘라 조회한다")
    void getTalentList_sizeClamp() {
        // given
        given(talentRepository.findTalentList(any(), anyInt(), any())).willReturn(List.of());

        // when: 99999 요청
        talentService.getTalentList(null, 99999, TalentSortType.LATEST);

        // then: 리포지토리엔 100으로 전달됐는지 캡처해서 확인
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(talentRepository).findTalentList(any(), sizeCaptor.capture(), any());
        assertThat(sizeCaptor.getValue()).isEqualTo(100);
    }

    @Test
    @DisplayName("size가 0 이하이면 최소 1로 보정한다")
    void getTalentList_sizeMin() {
        // given
        given(talentRepository.findTalentList(any(), anyInt(), any())).willReturn(List.of());

        // when
        talentService.getTalentList(null, 0, TalentSortType.LATEST);

        // then
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(talentRepository).findTalentList(any(), sizeCaptor.capture(), any());
        assertThat(sizeCaptor.getValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("getMyTalents - 인증 사용자 id로 조회해 결과를 그대로 반환한다")
    void getMyTalents_delegatesToRepository() {
        given(talentRepository.findMyTalents(1L)).willReturn(List.of(row(3L), row(1L)));
        List<TalentListRes> result = talentService.getMyTalents(1L);
        assertThat(result).extracting(TalentListRes::talentId).containsExactly(3L, 1L);
        verify(talentRepository).findMyTalents(1L);
    }

    // 테스트용 목록 항목 (id만 의미 있음 나머진 더미)
    private TalentListRes row(Long id) {
        return new TalentListRes(id, 1L, "user1", "백엔드", "제목" + id, 100, 2,
                BigDecimal.ZERO, 0, 0, LocalDateTime.now());
    }
}
