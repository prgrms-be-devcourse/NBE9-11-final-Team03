package com.back.baton.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청 DTO")
public record UserLoginReq(
        @Schema(
                description = "사용자 이메일",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED,
                format = "email"
        )
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(
                description = "비밀번호. 8자 이상 20자 이하",
                example = "password123",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 8,
                maxLength = 20,
                format = "password"
        )
        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 작성해야 합니다.")
        String password
) {
}
