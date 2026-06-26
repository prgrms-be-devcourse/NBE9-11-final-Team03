package com.back.baton.domain.admin.controller;

import com.back.baton.domain.admin.dto.request.AdminActionLogSearchReq;
import com.back.baton.domain.admin.dto.response.AdminActionLogRes;
import com.back.baton.domain.admin.dto.response.AdminPageRes;
import com.back.baton.domain.admin.service.AdminActionLogService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/action-logs")
@RequiredArgsConstructor
@Tag(name = "Admin Action Log", description = "관리자 조치 이력 API")
public class AdminActionLogController {

    private final AdminActionLogService adminActionLogService;

    @GetMapping
    @Operation(summary = "관리자 조치 이력 조회")
    public ResponseEntity<ApiResponse<AdminPageRes<AdminActionLogRes>>> getActionLogs(
            @ParameterObject @ModelAttribute AdminActionLogSearchReq req,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok(adminActionLogService.getActionLogs(req, pageable));
    }
}
