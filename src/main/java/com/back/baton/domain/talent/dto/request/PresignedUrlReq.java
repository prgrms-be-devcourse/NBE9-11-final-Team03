package com.back.baton.domain.talent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 발급 요청
public record PresignedUrlReq(
        @NotBlank(message = "파일 이름은 필수입니다.")
        String fileName,

        // 이미지 MIME 타입만 허용
        @NotBlank(message = "콘텐츠 타입은 필수입니다.")
        @Pattern(regexp = "^(?!image/svg\\+xml$)image/.+", message = "이미지 파일만 업로드할 수 있습니다. (SVG 제외)")
        String contentType
) {}