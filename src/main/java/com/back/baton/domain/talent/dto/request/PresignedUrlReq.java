package com.back.baton.domain.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 발급 요청
@Schema(description = "첨부 presigned URL 발급 요청 DTO")
public record PresignedUrlReq(
        @Schema(description = "원본 파일 이름", example = "photo.png", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "파일 이름은 필수입니다.")
        String fileName,

        @Schema(description = "이미지 MIME 타입. SVG 제외", example = "image/png", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "콘텐츠 타입은 필수입니다.")
        @Pattern(regexp = "^(?!image/svg\\+xml$)image/.+", message = "이미지 파일만 업로드할 수 있습니다. (SVG 제외)")
        String contentType
) {}