package com.back.baton.domain.trade.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Presigned URL 발급 요청 DTO")
public record PresignedUrlReq(
        @NotBlank
        @Schema(description = "업로드할 파일명 (확장자 포함)", example = "result.pdf")
        String fileName
) {
}