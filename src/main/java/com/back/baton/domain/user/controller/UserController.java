package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.service.UserService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import com.back.baton.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @CurrentUser SecurityUser currentUser,
            HttpServletResponse response
    ) {
        userService.withdraw(currentUser.getUserId());
        cookieUtil.setCookie(response, "refreshToken",null,0L); // 로그아웃 처리 - 쿠키 삭제

        return ApiResponses.success(SuccessCode.USER_WITHDRAW_SUCCESS, null);
    }

}
