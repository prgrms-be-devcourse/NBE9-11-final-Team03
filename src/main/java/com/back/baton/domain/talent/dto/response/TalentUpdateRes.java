// domain.talent.dto.response.TalentUpdateRes
package com.back.baton.domain.talent.dto.response;

import com.back.baton.domain.talent.entity.Talent;

public record TalentUpdateRes(
        Long talentId,
        Long categoryId,
        String title,
        String content,
        Integer estimatedHours,
        Integer creditPrice,
        String status
) {
    public static TalentUpdateRes from(Talent talent) {
        return new TalentUpdateRes(
                talent.getId(),
                talent.getCategory().getId(),
                talent.getTitle(),
                talent.getContent(),
                talent.getEstimatedHours(),
                talent.getCreditPrice(),
                talent.getStatus().name()
        );
    }
}