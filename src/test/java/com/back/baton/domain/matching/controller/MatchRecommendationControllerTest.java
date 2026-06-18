package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.service.MatchRecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchRecommendationController.class)
class MatchRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchRecommendationService matchRecommendationService;

    @Test
    @DisplayName("매칭 추천 상대 조회 API - 성공")
    void getMatchRecommendations_Success() throws Exception {
        Long talentId = 1L;
        Long userId = 2L;

        List<MatchRecommendationRes> res = List.of(
                new MatchRecommendationRes(
                        3L,
                        4L,
                        10L,
                        "백엔드",
                        "MySQL 튜닝 도와드립니다",
                        "MySQL 기본 쿼리와 인덱스를 도와드립니다.",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1
                )
        );

        when(matchRecommendationService.getMatchRecommendations(eq(talentId), eq(userId)))
                .thenReturn(res);

        mockMvc.perform(get("/api/v1/match-recommendations")
                        .param("talentId", String.valueOf(talentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-6"))
                .andExpect(jsonPath("$.message").value("매칭 추천 상대 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data[0].talentId").value(3L))
                .andExpect(jsonPath("$.data[0].providerId").value(4L))
                .andExpect(jsonPath("$.data[0].categoryId").value(10L))
                .andExpect(jsonPath("$.data[0].categoryName").value("백엔드"))
                .andExpect(jsonPath("$.data[0].title").value("MySQL 튜닝 도와드립니다"))
                .andExpect(jsonPath("$.data[0].creditPrice").value(120))
                .andExpect(jsonPath("$.data[0].estimatedHours").value(2))
                .andExpect(jsonPath("$.data[0].completeCount").value(1));

        verify(matchRecommendationService).getMatchRecommendations(talentId, userId);
    }
}