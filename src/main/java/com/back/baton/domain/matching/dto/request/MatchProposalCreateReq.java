package com.back.baton.domain.matching.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "매칭 제안 생성 요청 DTO")
public record MatchProposalCreateReq(

        @Schema(description = "요청자가 거래에 사용할 재능 ID. PURCHASE 흐름에서는 null일 수 있습니다.", example = "1")
        Long requesterTalentId,

        @Schema(description = "제공자 회원 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "제공자 회원 ID는 필수입니다.")
        Long providerId,

        @Schema(description = "제공자 재능 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "제공자 재능 ID는 필수입니다.")
        Long providerTalentId,

        @Schema(
                description = "매칭 제안 메시지. 1000자 이하",
                example = "해당 재능을 구매하고 싶습니다.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 1000
        )
        @NotBlank(message = "신청 메시지는 필수입니다.")
        @Size(max = 1000, message = "신청 메시지는 1000자 이하로 입력해 주세요.")
        String requestMessage
) {
}
