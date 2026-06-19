package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
                        1,
                        true,
                        null
                )
        );

        given(matchRecommendationService.getMatchRecommendations(talentId, userId))
                .willReturn(res);

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
                .andExpect(jsonPath("$.data[0].completeCount").value(1))
                .andExpect(jsonPath("$.data[0].proposalRequestEnabled").value(true))
                .andExpect(jsonPath("$.data[0].proposalRequestDisabledReason").isEmpty());

        then(matchRecommendationService).should()
                .getMatchRecommendations(talentId, userId);
    }

    @Test
    @DisplayName("매칭 추천 상대 상세 조회 API - 성공")
    void getMatchRecommendationDetail_success() throws Exception {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long userId = 1L;

        MatchRecommendationDetailRes response = new MatchRecommendationDetailRes(
                providerTalentId,
                3L,
                1L,
                "디자인",
                "Figma 와이어프레임 제작",
                "초기 서비스 아이디어를 Figma 저충실도 와이어프레임으로 만듭니다.",
                100,
                4,
                BigDecimal.valueOf(4.6),
                8,
                12,
                "이미자",
                "Figma 기반 와이어프레임과 포트폴리오 UI를 주로 작업합니다.",
                "https://example.com/profile.png",
                BigDecimal.valueOf(85),
                true,
                null
        );

        given(matchRecommendationService.getMatchRecommendationDetail(
                requesterTalentId,
                providerTalentId,
                userId
        )).willReturn(response);

        mockMvc.perform(get("/api/v1/match-recommendations/{providerTalentId}", providerTalentId)
                        .param("requesterTalentId", String.valueOf(requesterTalentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-7"))
                .andExpect(jsonPath("$.message").value("매칭 추천 상대 정보 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.talentId").value(providerTalentId))
                .andExpect(jsonPath("$.data.providerId").value(3L))
                .andExpect(jsonPath("$.data.categoryId").value(1L))
                .andExpect(jsonPath("$.data.categoryName").value("디자인"))
                .andExpect(jsonPath("$.data.title").value("Figma 와이어프레임 제작"))
                .andExpect(jsonPath("$.data.content").value("초기 서비스 아이디어를 Figma 저충실도 와이어프레임으로 만듭니다."))
                .andExpect(jsonPath("$.data.creditPrice").value(100))
                .andExpect(jsonPath("$.data.estimatedHours").value(4))
                .andExpect(jsonPath("$.data.avgRating").value(4.6))
                .andExpect(jsonPath("$.data.completeCount").value(8))
                .andExpect(jsonPath("$.data.viewCount").value(12))
                .andExpect(jsonPath("$.data.nickname").value("이미자"))
                .andExpect(jsonPath("$.data.introduction").value("Figma 기반 와이어프레임과 포트폴리오 UI를 주로 작업합니다."))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.png"))
                .andExpect(jsonPath("$.data.trustScore").value(85))
                .andExpect(jsonPath("$.data.proposalRequestEnabled").value(true))
                .andExpect(jsonPath("$.data.proposalRequestDisabledReason").isEmpty());

        then(matchRecommendationService).should()
                .getMatchRecommendationDetail(requesterTalentId, providerTalentId, userId);
    }
}