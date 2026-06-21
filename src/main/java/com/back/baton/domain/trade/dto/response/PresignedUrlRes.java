package com.back.baton.domain.trade.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 발급 응답 DTO")
public record PresignedUrlRes(
        @Schema(description = "S3 업로드용 Presigned PUT URL")
        String presignedUrl,

        @Schema(description = "업로드될 파일의 S3 Key (결과물 제출 시 사용)")
        String fileKey
) {
}