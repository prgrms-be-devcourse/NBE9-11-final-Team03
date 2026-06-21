package com.back.baton.domain.talent.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "첨부 presigned URL 발급 응답 DTO")
public record PresignedUrlRes(
        @Schema(description = "클라이언트가 PUT 할 presigned 업로드 URL")
        String uploadUrl,
        @Schema(description = "S3 객체 key. 업로드 후 저장 API에 그대로 전달", example = "talents/1/uuid-photo.png")
        String key
) {
    public static PresignedUrlRes of(String uploadUrl, String key) {
        return new PresignedUrlRes(uploadUrl, key);
    }
}