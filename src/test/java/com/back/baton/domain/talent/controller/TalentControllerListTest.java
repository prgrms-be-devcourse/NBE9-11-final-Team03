package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.response.CursorPageRes;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.service.TalentService;
import com.back.baton.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TalentController.class)
class TalentControllerListTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TalentService talentService;

    @MockitoBean // 또는 @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("목록 조회 성공 - 200과 커서 페이지 응답을 반환한다")
    void getTalentList_success() throws Exception {
        // given
        var item = new TalentListRes(5L, "백엔드", "스프링 리뷰", 100, 2,
                BigDecimal.valueOf(4.5), 3, 10, LocalDateTime.now());
        var page = CursorPageRes.of(List.of(item), true, 5L);
        given(talentService.getTalentList(any(), eq(20))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/talents")
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
    @DisplayName("cursor 없이 요청해도 정상 동작한다 (첫 페이지)")
    void getTalentList_noCursor() throws Exception {
        // given: cursor=null로 서비스 호출되는지
        var page = CursorPageRes.of(List.<TalentListRes>of(), false, null);
        given(talentService.getTalentList(eq(null), eq(20))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/talents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").isEmpty());
    }
}