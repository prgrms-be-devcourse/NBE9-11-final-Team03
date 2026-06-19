package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.response.CursorPageRes;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.service.TalentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TalentController.class)
class TalentControllerSearchTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TalentService talentService;

    @Test
    @DisplayName("검색 성공 - 200과 커서 페이지 응답을 반환한다")
    void searchTalents_success() throws Exception {
        // given
        var item = new TalentListRes(5L, "백엔드", "스프링 리뷰", 100, 2,
                BigDecimal.valueOf(4.5), 3, 10, LocalDateTime.now());
        var page = CursorPageRes.of(List.of(item), true, 5L);
        given(talentService.searchTalents(any(), any(), eq(20))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/talents/search")
                        .param("categoryId", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-2"))
                .andExpect(jsonPath("$.data.content[0].talentId").value(5))
                .andExpect(jsonPath("$.data.content[0].categoryName").value("백엔드"))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(5));
    }

    @Test
    @DisplayName("필터 4종 쿼리스트링이 TalentSearchReq에 바인딩된다")
    void searchTalents_bindsAllFilters() throws Exception {
        // given
        given(talentService.searchTalents(any(), any(), anyInt()))
                .willReturn(CursorPageRes.of(List.<TalentListRes>of(), false, null));

        // when: 필터 4종 + 완료여부 전부 전달
        mockMvc.perform(get("/api/v1/talents/search")
                        .param("categoryId", "1")
                        .param("minCredit", "100")
                        .param("maxCredit", "500")
                        .param("minRating", "4.0")
                        .param("completedOnly", "true"))
                .andExpect(status().isOk());

        // then: 쿼리스트링이 record에 제대로 매핑됐는지 캡처해서 검증
        ArgumentCaptor<TalentSearchReq> reqCaptor = ArgumentCaptor.forClass(TalentSearchReq.class);
        org.mockito.BDDMockito.then(talentService).should()
                .searchTalents(reqCaptor.capture(), any(), anyInt());

        TalentSearchReq req = reqCaptor.getValue();
        assertThat(req.categoryId()).isEqualTo(1L);
        assertThat(req.minCredit()).isEqualTo(100);
        assertThat(req.maxCredit()).isEqualTo(500);
        assertThat(req.minRating()).isEqualByComparingTo(BigDecimal.valueOf(4.0));
        assertThat(req.completedOnly()).isTrue();
    }

    @Test
    @DisplayName("필터 없이 요청해도 정상 동작한다 (전부 null 바인딩)")
    void searchTalents_noFilter() throws Exception {
        // given
        given(talentService.searchTalents(any(), any(), eq(20)))
                .willReturn(CursorPageRes.of(List.<TalentListRes>of(), false, null));

        // when & then: 파라미터 0개 -> record 전 필드 null, cursor null, size 기본 20
        mockMvc.perform(get("/api/v1/talents/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").isEmpty());

        ArgumentCaptor<TalentSearchReq> reqCaptor = ArgumentCaptor.forClass(TalentSearchReq.class);
        org.mockito.BDDMockito.then(talentService).should()
                .searchTalents(reqCaptor.capture(), any(), eq(20));
        assertThat(reqCaptor.getValue().categoryId()).isNull();
        assertThat(reqCaptor.getValue().completedOnly()).isNull();
    }

    @Test
    @DisplayName("/search가 상세 조회(/{talentId})로 잘못 매핑되지 않는다")
    void searchTalents_pathNotConfusedWithDetail() throws Exception {
        // given
        given(talentService.searchTalents(any(), any(), anyInt()))
                .willReturn(CursorPageRes.of(List.<TalentListRes>of(), false, null));

        // when & then: /search가 talentId="search"로 잡혀 500 나면 이 테스트가 잡아줌
        mockMvc.perform(get("/api/v1/talents/search"))
                .andExpect(status().isOk());
    }
}