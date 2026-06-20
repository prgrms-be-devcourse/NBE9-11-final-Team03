package com.back.baton.domain.talent.dto.response;

import com.back.baton.domain.talent.entity.TalentAttachment;

import java.time.LocalDateTime;

public record AttachmentRes(
        Long attachmentId,
        Long talentId,
        String url,
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