package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.dto.request.UserLoginReq;
import com.back.baton.domain.user.dto.request.UserSignupReq;
import com.back.baton.domain.user.dto.response.UserLoginRes;
import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.dto.response.UserTokenDto;
import com.back.baton.domain.user.service.UserService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.code.SuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserSignupRes>> signup(@Valid @RequestBody UserSignupReq req){

        UserSignupRes res = userService.signup(req.email(), req.password(), req.nickname(), req.introduction(),req.profileImageUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(SuccessCode.USER_SIGNUP_SUCCESS,res));
    }
    @PostMapping("/login")
    public ApiResponse<UserLoginRes> login(@Valid @RequestBody UserLoginReq req, HttpServletResponse response){

        UserTokenDto res = userService.login(req.email(), req.password());

        // accessToken -> 인메모리 저장, refreshToken -> 쿠키 저장

        long refreshTokenValidTime = 14 * 24 * 60 * 60 * 1000L; // 14일
        setCookie(response, "refreshToken", res.refreshToken(), refreshTokenValidTime);

        return ApiResponse.success(SuccessCode.USER_LOGIN_SUCCESS, new UserLoginRes(res.accessToken()));
    }

    @PostMapping("/reissue")
    public ApiResponse<UserLoginRes> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenValue,
            HttpServletResponse response
    ){
        UserTokenDto res = userService.reissue(refreshTokenValue);

        long refreshTokenValidTime = 14 * 24 * 60 * 60 * 1000L; // 14일
        setCookie(response, "refreshToken", res.refreshToken(), refreshTokenValidTime);

        return ApiResponse.success(SuccessCode.USER_REISSUE_SUCCESS, new UserLoginRes(res.accessToken()));
    }

    private void setCookie(HttpServletResponse response, String name, String value, Long maxAge){
        // 로그인 유지 별도 구현하는 경우 maxAge 설정
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict") // CSRF 방지 (Lax, Strict, None)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
