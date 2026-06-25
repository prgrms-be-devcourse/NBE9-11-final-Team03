package com.back.baton.domain.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "첨부 저장 요청 DTO")
public record AttachmentSaveReq(
        @Schema(description = "S3 업로드 후 받은 key 또는 외부 참고 링크 URL", example = "talents/1/uuid-photo.png", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
        @NotBlank(message = "URL은 필수입니다.")
        @Size(max = 500, message = "URL은 500자 이하여야 합니다.")
        String url,

        @Schema(description = "첨부 설명", example = "샘플 결과물 이미지", maxLength = 200)
        @Size(max = 200, message = "설명은 200자 이하여야 합니다.")
        String description
) {}