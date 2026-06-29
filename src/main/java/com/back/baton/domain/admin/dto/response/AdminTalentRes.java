package com.back.baton.domain.admin.dto.response;

import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import java.time.LocalDateTime;

// 관리자 재능 조회 응답.
public record AdminTalentRes(
        Long talentId,
        Long authorId,
        Long categoryId,
        String categoryName,
        String title,
        Integer estimatedHours,
        Integer creditPrice,
        TalentStatus status,
        int viewCount,
        int completeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminTalentRes from(Talent talent) {
        return new AdminTalentRes(
                talent.getId(),
                talent.getAuthorId(),
                talent.getCategory().getId(),
                talent.getCategory().getName(),
                talent.getTitle(),
                talent.getEstimatedHours(),
                talent.getCreditPrice(),
                talent.getStatus(),
                talent.getViewCount(),
                talent.getCompleteCount(),
                talent.getCreatedAt(),
                talent.getUpdatedAt()
        );
    }
}
