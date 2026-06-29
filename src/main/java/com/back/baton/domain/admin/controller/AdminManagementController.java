package com.back.baton.domain.admin.controller;

import com.back.baton.domain.admin.dto.request.AdminReportResolveReq;
import com.back.baton.domain.admin.dto.request.AdminReportSearchReq;
import com.back.baton.domain.admin.dto.request.AdminTalentSearchReq;
import com.back.baton.domain.admin.dto.request.AdminTalentStatusUpdateReq;
import com.back.baton.domain.admin.dto.request.AdminUserSearchReq;
import com.back.baton.domain.admin.dto.request.AdminUserStatusUpdateReq;
import com.back.baton.domain.admin.dto.response.AdminPageRes;
import com.back.baton.domain.admin.dto.response.AdminTalentReportRes;
import com.back.baton.domain.admin.dto.response.AdminTalentRes;
import com.back.baton.domain.admin.dto.response.AdminUserRes;
import com.back.baton.domain.admin.service.AdminManagementService;
import com.back.baton.domain.talent.dto.response.TalentDetailRes;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "관리자 유저/재능/신고 관리 API")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    @GetMapping("/users")
    @Operation(summary = "관리자 유저 목록 조회")
    public ResponseEntity<ApiResponse<AdminPageRes<AdminUserRes>>> getUsers(
            @Valid @ParameterObject @ModelAttribute AdminUserSearchReq req,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok(adminManagementService.getUsers(req, pageable));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "관리자 유저 상세 조회")
    public ResponseEntity<ApiResponse<AdminUserRes>> getUser(@PathVariable Long userId) {
        return ApiResponses.ok(adminManagementService.getUser(userId));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "관리자 유저 상태 변경")
    public ResponseEntity<ApiResponse<AdminUserRes>> updateUserStatus(
            @CurrentUser SecurityUser currentUser,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserStatusUpdateReq req
    ) {
        return ApiResponses.ok(adminManagementService.updateUserStatus(currentUser.getUserId(), userId, req));
    }

    @GetMapping("/talents")
    @Operation(summary = "관리자 재능 목록 조회")
    public ResponseEntity<ApiResponse<AdminPageRes<AdminTalentRes>>> getTalents(
            @Valid @ParameterObject @ModelAttribute AdminTalentSearchReq req,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok(adminManagementService.getTalents(req, pageable));
    }

    @GetMapping("/talents/{talentId}")
    @Operation(summary = "관리자 재능 상세 조회")
    public ResponseEntity<ApiResponse<TalentDetailRes>> getTalent(@PathVariable Long talentId) {
        return ApiResponses.ok(adminManagementService.getTalent(talentId));
    }

    @PatchMapping("/talents/{talentId}/status")
    @Operation(summary = "관리자 재능 상태 변경")
    public ResponseEntity<ApiResponse<AdminTalentRes>> updateTalentStatus(
            @CurrentUser SecurityUser currentUser,
            @PathVariable Long talentId,
            @Valid @RequestBody AdminTalentStatusUpdateReq req
    ) {
        return ApiResponses.ok(adminManagementService.updateTalentStatus(currentUser.getUserId(), talentId, req));
    }

    @GetMapping("/reports")
    @Operation(summary = "관리자 신고 목록 조회")
    public ResponseEntity<ApiResponse<AdminPageRes<AdminTalentReportRes>>> getReports(
            @Valid @ParameterObject @ModelAttribute AdminReportSearchReq req,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok(adminManagementService.getReports(req, pageable));
    }

    @GetMapping("/reports/{reportId}")
    @Operation(summary = "관리자 신고 상세 조회")
    public ResponseEntity<ApiResponse<AdminTalentReportRes>> getReport(@PathVariable Long reportId) {
        return ApiResponses.ok(adminManagementService.getReport(reportId));
    }

    @PatchMapping("/reports/{reportId}/resolve")
    @Operation(summary = "관리자 신고 처리 완료")
    public ResponseEntity<ApiResponse<AdminTalentReportRes>> resolveReport(
            @CurrentUser SecurityUser currentUser,
            @PathVariable Long reportId,
            @Valid @RequestBody AdminReportResolveReq req
    ) {
        return ApiResponses.ok(adminManagementService.resolveReport(currentUser.getUserId(), reportId, req));
    }
}
