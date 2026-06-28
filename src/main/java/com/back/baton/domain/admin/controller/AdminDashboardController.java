package com.back.baton.domain.admin.controller;

import com.back.baton.domain.admin.dto.response.AdminDashboardSummaryRes;
import com.back.baton.domain.admin.service.AdminDashboardService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "관리자 대시보드 요약 API")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @Operation(
            summary = "관리자 대시보드 요약 조회",
            description = "유저, 재능, 거래, 신고, 에스크로의 전체 개수와 상태별 개수를 조회합니다."
    )
    public ResponseEntity<ApiResponse<AdminDashboardSummaryRes>> getDashboardSummary() {
        return ApiResponses.ok(adminDashboardService.getDashboardSummary());
    }
}
