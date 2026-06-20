package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.dto.request.UserLoginReq;
import com.back.baton.domain.user.dto.request.UserSignupReq;
import com.back.baton.domain.user.dto.response.UserLoginRes;
import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.dto.response.UserTokenDto;
import com.back.baton.domain.user.service.UserService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth / User", description = "회원가입, 로그인, 토큰 재발급 API")
public class UserController {

    private final UserService userService;

    @Value("${cookie.secure:true}")
    private boolean isSecure;

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임, 프로필 이미지 URL, 한줄 소개를 입력해 신규 사용자를 생성합니다."
    )
    public ResponseEntity<ApiResponse<UserSignupRes>> signup(@Valid @RequestBody UserSignupReq req) {
        UserSignupRes res = userService.signup(
                req.email(),
                req.password(),
                req.nickname(),
                req.introduction(),
                req.profileImageUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.USER_SIGNUP_SUCCESS, res));
    }

    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 accessToken을 응답합니다. refreshToken은 HttpOnly 쿠키로 저장합니다."
    )
    public ApiResponse<UserLoginRes> login(@Valid @RequestBody UserLoginReq req, HttpServletResponse response) {
        UserTokenDto res = userService.login(req.email(), req.password());

        long refreshTokenValidTime = 14 * 24 * 60 * 60 * 1000L;
        setCookie(response, "refreshToken", res.refreshToken(), refreshTokenValidTime);

        return ApiResponse.success(SuccessCode.USER_LOGIN_SUCCESS, new UserLoginRes(res.accessToken()));
    }

    @PostMapping("/reissue")
    @Operation(
            summary = "토큰 재발급",
            description = "refreshToken 쿠키를 검증해 accessToken을 재발급하고 refreshToken도 함께 갱신합니다."
    )
    public ApiResponse<UserLoginRes> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenValue,
            HttpServletResponse response
    ) {
        UserTokenDto res = userService.reissue(refreshTokenValue);

        long refreshTokenValidTime = 14 * 24 * 60 * 60 * 1000L;
        setCookie(response, "refreshToken", res.refreshToken(), refreshTokenValidTime);

        return ApiResponse.success(SuccessCode.USER_REISSUE_SUCCESS, new UserLoginRes(res.accessToken()));
    }

    private void setCookie(HttpServletResponse response, String name, String value, Long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
