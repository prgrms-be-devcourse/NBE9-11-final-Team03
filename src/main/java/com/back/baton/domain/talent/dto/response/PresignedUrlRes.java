package com.back.baton.domain.talent.dto.response;

public record PresignedUrlRes(
        String uploadUrl,   // 클라가 PUT 할 presigned URL
        String key          // S3 객체 key (업로드 후 저장 API에 그대로 전달)
) {
    public static PresignedUrlRes of(String uploadUrl, String key) {
        return new PresignedUrlRes(uploadUrl, key);
    }
}