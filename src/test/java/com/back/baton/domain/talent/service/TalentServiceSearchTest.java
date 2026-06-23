package com.back.baton.domain.talent.service;

import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.response.TalentListRes;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TalentServiceSearchTest {

    @InjectMocks TalentService talentService;
    @Mock TalentRepository talentRepository;

    @Test
    @DisplayName("size+1개가 조회되면 hasNext=true, size개로 자르고 마지막 id를 nextCursor로 준다")
    void searchTalents_hasNext() {
        // given: size=2인데 리포지토리가 3개(size+1) 반환
        int size = 2;
        TalentSearchReq req = new TalentSearchReq(1L, null, null, null, null);
        given(talentRepository.searchTalents(any(), any(), anyInt()))
                .willReturn(List.of(row(5L), row(4L), row(3L)));

        // when
        CursorPageRes<TalentListRes> result = talentService.searchTalents(req, null, size);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).extracting(TalentListRes::talentId)
                .containsExactly(5L, 4L);
        assertThat(result.nextCursor()).isEqualTo(4L);
    }

    @Test
    @DisplayName("size 이하로 조회되면 hasNext=false, nextCursor=null")
    void searchTalents_lastPage() {
        int size = 2;
        TalentSearchReq req = new TalentSearchReq(null, null, null, null, null);
        given(talentRepository.searchTalents(any(), any(), anyInt()))
                .willReturn(List.of(row(2L), row(1L)));

        CursorPageRes<TalentListRes> result = talentService.searchTalents(req, null, size);

        assertThat(result.hasNext()).isFalse();
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("size가 상한(100)을 넘으면 100으로 잘라 조회한다")
    void searchTalents_sizeClamp() {
        TalentSearchReq req = new TalentSearchReq(null, null, null, null, null);
        given(talentRepository.searchTalents(any(), any(), anyInt())).willReturn(List.of());

        talentService.searchTalents(req, null, 99999);

        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(talentRepository).searchTalents(any(), any(), sizeCaptor.capture());
        assertThat(sizeCaptor.getValue()).isEqualTo(100);
    }

    @Test
    @DisplayName("검색 조건(req)이 그대로 리포지토리로 전달된다")
    void searchTalents_passesCondition() {
        TalentSearchReq req = new TalentSearchReq(1L, 100, 500, BigDecimal.valueOf(4.0), true);
        given(talentRepository.searchTalents(any(), any(), anyInt())).willReturn(List.of());

        talentService.searchTalents(req, 10L, 20);

        ArgumentCaptor<TalentSearchReq> reqCaptor = ArgumentCaptor.forClass(TalentSearchReq.class);
        verify(talentRepository).searchTalents(reqCaptor.capture(), any(), anyInt());
        assertThat(reqCaptor.getValue().categoryId()).isEqualTo(1L);
        assertThat(reqCaptor.getValue().completedOnly()).isTrue();
    }

    private TalentListRes row(Long id) {
        return new TalentListRes(id, "백엔드", "제목" + id, 100, 2,
                BigDecimal.ZERO, 0, 0, LocalDateTime.now());
    }
}