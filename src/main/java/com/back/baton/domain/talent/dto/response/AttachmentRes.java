package com.back.baton.domain.talent.dto.response;

import com.back.baton.domain.talent.entity.TalentAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "첨부 응답 DTO")
public record AttachmentRes(
        @Schema(description = "첨부 ID", example = "10")
        Long attachmentId,
        @Schema(description = "재능 ID", example = "1")
        Long talentId,
        @Schema(description = "표시용 URL. S3 key는 presigned GET, 외부 링크는 원본 그대로")
        String url,
        @Schema(description = "첨부 설명", example = "샘플 결과물 이미지")
        String description,
        LocalDateTime createdAt
) {
    public static AttachmentRes from(TalentAttachment attachment) {
        return new AttachmentRes(
                attachment.getId(),
                attachment.getTalent().getId(),
                attachment.getUrl(),
                attachment.getDescription(),
                attachment.getCreatedAt()
        );
    }
    // 표시용 URL(presigned GET 또는 외부링크)을 주입받아 노출
    public static AttachmentRes of(TalentAttachment attachment, String displayUrl) {
        return new AttachmentRes(
                attachment.getId(),
                attachment.getTalent().getId(),
                displayUrl,
                attachment.getDescription(),
                attachment.getCreatedAt()
        );
    }
}