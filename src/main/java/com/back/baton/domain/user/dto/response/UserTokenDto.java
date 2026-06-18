package com.back.baton.domain.user.dto.response;

public record UserTokenDto(
        String accessToken,
        String refreshToken
) { }