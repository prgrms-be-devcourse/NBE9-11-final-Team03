package com.back.baton.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserEmailVerificationReq(
        @Schema(
                description = "인증할 이메일",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED,
                format = "email"
        )
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(
                description = "이메일로 받은 6자리 인증 코드",
                example = "123456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "인증 코드는 필수 입력 항목입니다.")
        @Pattern(regexp = "\\d{6}", message = "인증 코드는 6자리 숫자여야 합니다.")
        String verificationCode
) {
}
