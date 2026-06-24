package com.back.baton.domain.user.dto;

import java.time.LocalDateTime;

public record EmailVerification(
        String code,
        LocalDateTime expiredAt,
        boolean verified
) { }