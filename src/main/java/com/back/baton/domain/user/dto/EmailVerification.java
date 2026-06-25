package com.back.baton.domain.user.dto;

import java.time.LocalDateTime;

public record EmailVerification(
        String code,
        LocalDateTime expiredAt,
        boolean verified,
        int attempts
) {
    public EmailVerification markVerified(){
        return new EmailVerification(code, expiredAt, true, attempts);
    }

    public EmailVerification increaseAttempts(){
        return new EmailVerification(code, expiredAt, verified, attempts + 1);
    }
}