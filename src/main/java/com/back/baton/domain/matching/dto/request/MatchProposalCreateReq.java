package com.back.baton.domain.matching.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MatchProposalCreateReq(

        Long requesterTalentId,

        @NotNull(message = "제공자 회원 ID는 필수입니다.")
        Long providerId,

        @NotNull(message = "제공자 재능 ID는 필수입니다.")
        Long providerTalentId,

        @NotBlank(message = "신청 메시지는 필수입니다.")
        @Size(max = 1000, message = "신청 메시지는 1000자 이하로 입력해 주세요.")
        String requestMessage
) {
}