package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.service.MatchRecommendationService;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.support.security.WithMockSecurityUser;
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

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("매칭 추천 목록 조회 API - 인증 사용자 ID를 사용한다")
    @WithMockSecurityUser(userId = 2)
    void getMatchRecommendations_Success() throws Exception {
        Long userId = 2L;

        List<MatchRecommendationRes> res = List.of(
                new MatchRecommendationRes(
                        3L,
                        1L,
                        4L,
                        10L,
                        "Design",
                        "Figma lesson",
                        "Figma wireframe lesson",
                        120,
                        2,
                        BigDecimal.valueOf(4.00),
                        1,
                        true,
                        null
                )
        );

        given(matchRecommendationService.getMatchRecommendations(userId))
                .willReturn(res);

        mockMvc.perform(get("/api/v1/match-recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-6"))
                .andExpect(jsonPath("$.data[0].talentId").value(3L))
                .andExpect(jsonPath("$.data[0].requesterTalentId").value(1L))
                .andExpect(jsonPath("$.data[0].providerId").value(4L))
                .andExpect(jsonPath("$.data[0].categoryId").value(10L))
                .andExpect(jsonPath("$.data[0].categoryName").value("Design"))
                .andExpect(jsonPath("$.data[0].title").value("Figma lesson"))
                .andExpect(jsonPath("$.data[0].creditPrice").value(120))
                .andExpect(jsonPath("$.data[0].estimatedHours").value(2))
                .andExpect(jsonPath("$.data[0].completeCount").value(1))
                .andExpect(jsonPath("$.data[0].proposalRequestEnabled").value(true))
                .andExpect(jsonPath("$.data[0].proposalRequestDisabledReason").isEmpty());

        then(matchRecommendationService).should()
                .getMatchRecommendations(userId);
    }

    @Test
    @DisplayName("매칭 추천 상세 조회 API - 인증 사용자 ID를 사용한다")
    @WithMockSecurityUser(userId = 1)
    void getMatchRecommendationDetail_success() throws Exception {
        Long requesterTalentId = 1L;
        Long providerTalentId = 2L;
        Long userId = 1L;

        MatchRecommendationDetailRes response = new MatchRecommendationDetailRes(
                providerTalentId,
                requesterTalentId,
                3L,
                1L,
                "Design",
                "Figma lesson",
                "Figma wireframe lesson",
                100,
                4,
                BigDecimal.valueOf(4.6),
                8,
                12,
                "designer",
                "Figma portfolio UI",
                "https://example.com/profile.png",
                BigDecimal.valueOf(85),
                true,
                null
        );

        given(matchRecommendationService.getMatchRecommendationDetail(
                providerTalentId,
                userId
        )).willReturn(response);

        mockMvc.perform(get("/api/v1/match-recommendations/{providerTalentId}", providerTalentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-7"))
                .andExpect(jsonPath("$.data.talentId").value(providerTalentId))
                .andExpect(jsonPath("$.data.requesterTalentId").value(requesterTalentId))
                .andExpect(jsonPath("$.data.providerId").value(3L))
                .andExpect(jsonPath("$.data.categoryId").value(1L))
                .andExpect(jsonPath("$.data.categoryName").value("Design"))
                .andExpect(jsonPath("$.data.title").value("Figma lesson"))
                .andExpect(jsonPath("$.data.creditPrice").value(100))
                .andExpect(jsonPath("$.data.estimatedHours").value(4))
                .andExpect(jsonPath("$.data.avgRating").value(4.6))
                .andExpect(jsonPath("$.data.completeCount").value(8))
                .andExpect(jsonPath("$.data.viewCount").value(12))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.png"))
                .andExpect(jsonPath("$.data.trustScore").value(85))
                .andExpect(jsonPath("$.data.proposalRequestEnabled").value(true))
                .andExpect(jsonPath("$.data.proposalRequestDisabledReason").isEmpty());

        then(matchRecommendationService).should()
                .getMatchRecommendationDetail(providerTalentId, userId);
    }
}
