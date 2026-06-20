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
}