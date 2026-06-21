package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.service.UserService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
@Tag(name = "User", description = "탈퇴, 내 정보 조회 API")
public class UserController {
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @DeleteMapping
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response
    ) {
        userService.withdraw(Long.parseLong(userDetails.getUsername()));
        cookieUtil.setCookie(response, "refreshToken",null,0L); // 로그아웃 처리 - 쿠키 삭제

        return ApiResponse.success(SuccessCode.USER_WITHDRAW_SUCCESS, null);
    }

}
