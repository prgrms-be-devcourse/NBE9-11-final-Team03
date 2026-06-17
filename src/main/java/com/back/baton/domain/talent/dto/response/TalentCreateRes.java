package com.back.baton.domain.talent.dto.response;

import com.back.baton.domain.talent.entity.Talent;

public record TalentCreateRes(Long talentId) {
    public static TalentCreateRes from(Talent talent) {
        return new TalentCreateRes(talent.getId());
    }
}