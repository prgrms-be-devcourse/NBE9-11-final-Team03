package com.back.baton.domain.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    // accessToken 만료 시간: 15분
    private final long accessTokenValidTime = 15 * 60 * 1000L;
    // refreshToken 만료 시간: 14일
    private final long refreshTokenValidTime = 14 * 24 * 60 * 60 * 1000L;

    public String createAccessToken(Long userId, String role, Date now){
        return JWT.create()
                .withSubject(userId.toString()) // 페이로드: 유저 식별자
                .withClaim("role", role)   // 페이로드: 커스텀 권한
                .withIssuedAt(now) // 페이로드: 시작 시간
                .withExpiresAt(new Date(now.getTime() + accessTokenValidTime)) // 페이로드: 만료 시간
                .sign(Algorithm.HMAC256(secretKey));
    }

    public String createRefreshToken(Long userId, Date now){
        return JWT.create()
                .withSubject(userId.toString()) // 페이로드: 유저 식별자
                .withIssuedAt(now) // 페이로드: 시작 시간
                .withExpiresAt(new Date(now.getTime() + refreshTokenValidTime)) // 페이로드: 만료 시간
                .sign(Algorithm.HMAC256(secretKey));
    }
}
