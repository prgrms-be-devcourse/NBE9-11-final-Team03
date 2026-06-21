// domain.talent.controller.TalentAttachmentController
package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.AttachmentSaveReq;
import com.back.baton.domain.talent.dto.request.PresignedUrlReq;
import com.back.baton.domain.talent.dto.response.AttachmentRes;
import com.back.baton.domain.talent.dto.response.PresignedUrlRes;
import com.back.baton.domain.talent.service.TalentAttachmentService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            description = "본인 재능에 대해 S3 직접 업로드용 presigned PUT URL과 객체 key를 발급합니다. 클라이언트가 이 URL로 S3에 직접 PUT 합니다."
    )
    public ResponseEntity<ApiResponse<PresignedUrlRes>> createPresignedUrl(
            @Parameter(description = "재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @Parameter(description = "작성자 회원 ID. 인증 연동 전까지 X-User-Id 헤더로 전달합니다.", example = "7", required = true)
            @RequestHeader("X-User-Id") Long authorId,
            @Valid @RequestBody PresignedUrlReq req) {

        PresignedUrlRes response = talentAttachmentService.createPresignedUrl(talentId, authorId, req);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, response);
    }

    @PostMapping
    @Operation(
            summary = "첨부 저장",
            description = "S3 업로드 후 받은 key 또는 외부 참고 링크 URL을 DB에 기록합니다. 응답 url은 표시용(presigned GET 또는 외부 링크)으로 반환합니다."
    )
    public ResponseEntity<ApiResponse<AttachmentRes>> saveAttachment(
            @Parameter(description = "재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @Parameter(description = "작성자 회원 ID. 인증 연동 전까지 X-User-Id 헤더로 전달합니다.", example = "7", required = true)
            @RequestHeader("X-User-Id") Long authorId, // TODO: @AuthenticationPrincipal
            @Valid @RequestBody AttachmentSaveReq request) {

        AttachmentRes response = talentAttachmentService.saveAttachment(talentId, authorId, request);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_CREATED, response);
    }

    @GetMapping
    @Operation(
            summary = "첨부 목록 조회",
            description = "재능의 첨부 목록을 id 오름차순으로 조회합니다. 공개 API로 인증 헤더가 필요 없습니다. url은 표시용으로 변환되어 반환됩니다."
    )
    public ResponseEntity<ApiResponse<List<AttachmentRes>>> getAttachments(
            @Parameter(description = "재능 ID", example = "1", required = true)
            @PathVariable Long talentId) {

        List<AttachmentRes> response = talentAttachmentService.getAttachments(talentId);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, response);
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(
            summary = "첨부 삭제",
            description = "본인 재능의 첨부를 삭제합니다. DB 레코드 삭제 후 S3 업로드분은 실제 객체도 삭제합니다(외부 링크는 스킵)."
    )
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @Parameter(description = "재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @Parameter(description = "첨부 ID", example = "10", required = true)
            @PathVariable Long attachmentId,
            @Parameter(description = "작성자 회원 ID. 인증 연동 전까지 X-User-Id 헤더로 전달합니다.", example = "7", required = true)
            @RequestHeader("X-User-Id") Long authorId) { // TODO: @AuthenticationPrincipal

        talentAttachmentService.deleteAttachment(talentId, attachmentId, authorId);
        return ApiResponses.success(SuccessCode.TALENT_ATTACHMENT_OK, null);
    }
}