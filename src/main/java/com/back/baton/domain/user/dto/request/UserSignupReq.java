package com.back.baton.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSignupReq(
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 작성해야 합니다." )
        String password,

        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        @Size(min = 3, max = 10, message = "닉네임은 3자 이상, 10자 이하로 작성해야 합니다.")
        String nickname,

        String profileImageUrl,

        @NotBlank(message = "한줄 설명은 필수 입력 항목입니다.")
        @Size(min = 5, message = "한줄 설명은 5자 이상으로 작성해야 합니다.")
        String introduction
) {
}
