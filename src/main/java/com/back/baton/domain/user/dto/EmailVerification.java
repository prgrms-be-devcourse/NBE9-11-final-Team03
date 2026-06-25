package com.back.baton.domain.user.dto;

import java.time.LocalDateTime;

public record EmailVerification(
        String code,
        LocalDateTime expiredAt,
        boolean verified
) {
    public EmailVerification markVerified(){
        return new EmailVerification(code, expiredAt, true);
    }
}