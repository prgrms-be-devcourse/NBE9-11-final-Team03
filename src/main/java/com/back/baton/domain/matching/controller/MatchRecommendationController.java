package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.service.MatchRecommendationService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
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
@Tag(name = "Matching / Recommendation", description = "매칭 추천 상대 목록 및 상세 조회 API")
public class MatchRecommendationController {

    private final MatchRecommendationService matchRecommendationService;

    @GetMapping
    @Operation(
            summary = "매칭 추천 상대 목록 조회",
            description = "요청자의 재능과 같은 카테고리에 속한 추천 상대 재능 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<MatchRecommendationRes>>> getMatchRecommendations(
            @Parameter(description = "요청자의 재능 ID", example = "1", required = true)
            @RequestParam Long talentId,
            @Parameter(description = "요청자 회원 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "1", required = true)
            @RequestParam Long userId
    ) {
        List<MatchRecommendationRes> response = matchRecommendationService.getMatchRecommendations(talentId, userId);

        return ApiResponses.success(SuccessCode.MATCH_RECOMMENDATIONS_FOUND, response);
    }

    @GetMapping("/{providerTalentId}")
    @Operation(
            summary = "매칭 추천 상대 상세 조회",
            description = "추천 상대의 재능 정보, 제공자 프로필 정보, 통계 정보와 제안 가능 여부를 조회합니다."
    )
    public ResponseEntity<ApiResponse<MatchRecommendationDetailRes>> getMatchRecommendationDetail(
            @Parameter(description = "추천 상대의 재능 ID", example = "2", required = true)
            @PathVariable Long providerTalentId,
            @Parameter(description = "요청자의 재능 ID", example = "1", required = true)
            @RequestParam Long requesterTalentId,
            @Parameter(description = "요청자 회원 ID. 인증 연동 전까지 query parameter로 전달합니다.", example = "1", required = true)
            @RequestParam Long userId
    ) {
        MatchRecommendationDetailRes response =
                matchRecommendationService.getMatchRecommendationDetail(
                        requesterTalentId,
                        providerTalentId,
                        userId
                );

        return ApiResponses.success(SuccessCode.MATCH_RECOMMENDATION_DETAIL_FOUND, response);
    }
}
