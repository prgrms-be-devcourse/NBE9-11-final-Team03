package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.service.MatchRecommendationService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/match-recommendations")
@RequiredArgsConstructor
@Tag(name = "Matching / Recommendation", description = "매칭 추천 목록 및 상세 조회 API")
public class MatchRecommendationController {

    private final MatchRecommendationService matchRecommendationService;

    @GetMapping
    @Operation(
            summary = "매칭 추천 대상 목록 조회",
            description = "현재 로그인한 사용자의 등록된 재능 카테고리와 프로필의 원하는 재능 카테고리를 기준으로 추천 재능 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<MatchRecommendationRes>>> getMatchRecommendations(
            @CurrentUser SecurityUser currentUser
    ) {
        Long userId = currentUser.getUserId();
        List<MatchRecommendationRes> response = matchRecommendationService.getMatchRecommendations(userId);

        return ApiResponses.success(SuccessCode.MATCH_RECOMMENDATIONS_FOUND, response);
    }

    @GetMapping("/{providerTalentId}")
    @Operation(
            summary = "매칭 추천 대상 상세 조회",
            description = "추천 대상의 재능 정보, 제공자 프로필 정보, 통계 정보와 제안 가능 여부를 조회합니다."
    )
    public ResponseEntity<ApiResponse<MatchRecommendationDetailRes>> getMatchRecommendationDetail(
            @Parameter(description = "추천 대상 재능 ID", example = "2", required = true)
            @PathVariable Long providerTalentId,
            @Parameter(description = "요청자의 재능 ID", example = "1", required = true)
            @RequestParam Long requesterTalentId,
            @CurrentUser SecurityUser currentUser
    ) {
        Long userId = currentUser.getUserId();
        MatchRecommendationDetailRes response =
                matchRecommendationService.getMatchRecommendationDetail(
                        requesterTalentId,
                        providerTalentId,
                        userId
                );

        return ApiResponses.success(SuccessCode.MATCH_RECOMMENDATION_DETAIL_FOUND, response);
    }
}
