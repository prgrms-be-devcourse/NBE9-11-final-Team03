package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.AttachmentSaveReq;
import com.back.baton.domain.talent.dto.request.PresignedUrlReq;
import com.back.baton.domain.talent.dto.response.AttachmentRes;
import com.back.baton.domain.talent.dto.response.PresignedUrlRes;
import com.back.baton.domain.talent.service.TalentAttachmentService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/talents/{talentId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Talent / Attachment", description = "재능 첨부 presigned URL 발급, 저장, 목록, 삭제 API")
public class TalentAttachmentController {

    private final TalentAttachmentService talentAttachmentService;

    @PostMapping("/presigned-url")
    @Operation(
            summary = "첨부 업로드 presigned URL 발급",
            description = "현재 로그인한 작성자의 재능에 대해 S3 직접 업로드용 presigned PUT URL과 객체 key를 발급합니다."
    )
    public ResponseEntity<ApiResponse<PresignedUrlRes>> createPresignedUrl(
            @Parameter(description = "재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @CurrentUser SecurityUser currentUser,
            @Valid @RequestBody PresignedUrlReq req
    ) {
        PresignedUrlRes response = talentAttachmentService.createPresignedUrl(
                talentId,
                currentUser.getUserId(),
                req
        );
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, response);
    }

    @PostMapping
    @Operation(
            summary = "첨부 저장",
            description = "현재 로그인한 작성자의 재능에 S3 업로드 key 또는 외부 참고 링크 URL을 DB에 기록합니다."
    )
    public ResponseEntity<ApiResponse<AttachmentRes>> saveAttachment(
            @Parameter(description = "재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @CurrentUser SecurityUser currentUser,
            @Valid @RequestBody AttachmentSaveReq request
    ) {
        AttachmentRes response = talentAttachmentService.saveAttachment(
                talentId,
                currentUser.getUserId(),
                request
        );
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_CREATED, response);
    }

    @GetMapping
    @Operation(
            summary = "첨부 목록 조회",
            description = "재능의 첨부 목록을 id 오름차순으로 조회합니다. 공개 API로 인증 헤더가 필요 없습니다."
    )
    public ResponseEntity<ApiResponse<List<AttachmentRes>>> getAttachments(
            @Parameter(description = "재능 ID", example = "1", required = true)
            @PathVariable Long talentId
    ) {
        List<AttachmentRes> response = talentAttachmentService.getAttachments(talentId);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, response);
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(
            summary = "첨부 삭제",
            description = "현재 로그인한 작성자가 본인 재능의 첨부를 삭제합니다. S3 업로드분은 실제 객체도 삭제합니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @Parameter(description = "재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @Parameter(description = "첨부 ID", example = "10", required = true)
            @PathVariable Long attachmentId,
            @CurrentUser SecurityUser currentUser
    ) {
        talentAttachmentService.deleteAttachment(talentId, attachmentId, currentUser.getUserId());
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, null);
    }
}
