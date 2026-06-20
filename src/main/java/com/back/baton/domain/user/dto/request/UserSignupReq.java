package com.back.baton.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 DTO")
public record UserSignupReq(
        @Schema(description = "사용자 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "비밀번호. 8자 이상 20자 이하", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 작성해야 합니다.")
        String password,

        @Schema(description = "닉네임. 3자 이상 10자 이하", example = "바톤러", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        @Size(min = 3, max = 10, message = "닉네임은 3자 이상, 10자 이하로 작성해야 합니다.")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
        String profileImageUrl,

        @Schema(description = "사용자 한줄 소개. 5자 이상", example = "백엔드 개발을 도와드립니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "한줄 설명은 필수 입력 항목입니다.")
        @Size(min = 5, message = "한줄 설명은 5자 이상으로 작성해야 합니다.")
        String introduction
) {
}
