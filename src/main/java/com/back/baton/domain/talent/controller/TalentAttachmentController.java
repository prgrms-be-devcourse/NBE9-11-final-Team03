package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.AttachmentSaveReq;
import com.back.baton.domain.talent.dto.request.PresignedUrlReq;
import com.back.baton.domain.talent.dto.response.AttachmentRes;
import com.back.baton.domain.talent.dto.response.PresignedUrlRes;
import com.back.baton.domain.talent.service.TalentAttachmentService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/talents/{talentId}/attachments")
@RequiredArgsConstructor
public class TalentAttachmentController {

    private final TalentAttachmentService talentAttachmentService;

    // Presigned URL 발급 (클라가 이 URL로 S3에 직접 PUT)
    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlRes>> createPresignedUrl(
            @PathVariable Long talentId,
            @RequestHeader("X-User-Id") Long authorId,
            @Valid @RequestBody PresignedUrlReq req) {

        PresignedUrlRes response = talentAttachmentService.createPresignedUrl(talentId, authorId, req);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, response);
    }

    // 첨부 저장 (업로드된 key 또는 외부 링크 url을 DB에 기록)
    @PostMapping
    public ResponseEntity<ApiResponse<AttachmentRes>> saveAttachment(
            @PathVariable Long talentId,
            @RequestHeader("X-User-Id") Long authorId, // TODO: @AuthenticationPrincipal
            @Valid @RequestBody AttachmentSaveReq request) {

        AttachmentRes response = talentAttachmentService.saveAttachment(talentId, authorId, request);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_CREATED, response);
    }

    // 첨부 목록 조회 (공개)
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttachmentRes>>> getAttachments(
            @PathVariable Long talentId) {

        List<AttachmentRes> response = talentAttachmentService.getAttachments(talentId);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, response);
    }

    // 첨부 삭제 (본인 재능의 첨부만)
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long talentId,
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") Long authorId) { // TODO: @AuthenticationPrincipal

        talentAttachmentService.deleteAttachment(talentId, attachmentId, authorId);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, null);
    }
}