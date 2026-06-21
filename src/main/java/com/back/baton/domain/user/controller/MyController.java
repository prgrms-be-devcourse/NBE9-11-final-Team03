package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.service.UserService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
@Tag(name = "My", description = "탈퇴, 내 정보 조회 API")
@Transactional
public class MyController {
    private final UserService userService;

    @DeleteMapping
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.withdraw(Long.parseLong(userDetails.getUsername()));
        return ApiResponse.success(SuccessCode.USER_REISSUE_SUCCESS, null);
    }

}
