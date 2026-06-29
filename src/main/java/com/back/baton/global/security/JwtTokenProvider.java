package com.back.baton.global.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TokenErrorCode;
import jakarta.servlet.http.HttpServletRequest;
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

    public Long getUserIdFromToken(String tokenValue){
        Long userId = null;
        try{
            userId = Long.parseLong(JWT.decode(tokenValue).getSubject());
        }catch (Exception e){
            throw new CustomException(TokenErrorCode.INVALID_TOKEN);
        }
        if(userId == null){
            throw new CustomException(TokenErrorCode.INVALID_TOKEN);
        }
        return userId;
    }
    public void validateToken(String tokenValue){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secretKey.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build(); // 검증기 생성

            verifier.verify(tokenValue); // 서명, 알고리즘, 유효시간 검증
        }catch (Exception e){
           throw new CustomException(TokenErrorCode.INVALID_TOKEN);
        }
    }

    public DecodedJWT resolveToken(HttpServletRequest request) {
        return resolveToken(request.getHeader("Authorization"));
    }

    public DecodedJWT resolveToken(String authorization){ // 헤더에서 accessToken 추출 및 검증, 반환
        String accessToken = null;

        if(authorization == null || authorization.isEmpty()){
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            return null;
        }
        accessToken = authorization.substring("Bearer ".length());

        try{
            Algorithm algorithm = Algorithm.HMAC256(secretKey.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build(); // 검증기 생성
            return verifier.verify(accessToken); // 서명, 알고리즘, 유효시간 검증 및 반환
        }catch (Exception e){
            return null;
        }
    }
}
