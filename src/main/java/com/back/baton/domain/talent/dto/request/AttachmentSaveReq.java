package com.back.baton.domain.talent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 저장 요청
public record AttachmentSaveReq(
        // S3 업로드 후 받은 key, 또는 외부 참고 링크 URL
        @NotBlank(message = "URL은 필수입니다.")
        @Size(max = 500, message = "URL은 500자 이하여야 합니다.")
        String url,

        @Size(max = 200, message = "설명은 200자 이하여야 합니다.")
        String description
) {}