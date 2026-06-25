package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.TalentReportReq;
import com.back.baton.domain.talent.dto.response.TalentReportRes;
import com.back.baton.domain.talent.service.TalentReportService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/talents/{talentId}/reports")
@RequiredArgsConstructor
@Tag(name = "Talent / Report", description = "재능 신고 API")
public class TalentReportController {

    private final TalentReportService talentReportService;

    @PostMapping
    @Operation(
            summary = "재능 신고",
            description = "현재 로그인한 사용자가 부적절한 재능을 신고합니다. 본인 재능 신고와 중복 신고는 차단됩니다."
    )
    public ResponseEntity<ApiResponse<TalentReportRes>> reportTalent(
            @Parameter(description = "신고할 재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @CurrentUser SecurityUser currentUser,
            @Valid @RequestBody TalentReportReq request
    ) {
        TalentReportRes response = talentReportService.reportTalent(
                talentId,
                currentUser.getUserId(),
                request
        );
        return ApiResponses.success(SuccessCode.TALENT_REPORT_CREATED, response);
    }
}