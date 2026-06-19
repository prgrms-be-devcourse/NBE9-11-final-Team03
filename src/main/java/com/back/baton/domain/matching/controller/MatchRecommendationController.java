package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.service.MatchRecommendationService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/match-recommendations")
@RequiredArgsConstructor
public class MatchRecommendationController {

    private final MatchRecommendationService matchRecommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchRecommendationRes>>> getMatchRecommendations(
            @RequestParam Long talentId,
            @RequestParam Long userId // TODO: 인증 연동 후 로그인 사용자 ID로 대체
    ) {
        List<MatchRecommendationRes> response = matchRecommendationService.getMatchRecommendations(talentId, userId);

        return ApiResponses.success(SuccessCode.MATCH_RECOMMENDATIONS_FOUND, response);
    }
}