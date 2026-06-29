package com.back.baton.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCheckNicknameReq(
        @NotBlank
        @Size(min = 3, max = 10)
        String nickname
){}
