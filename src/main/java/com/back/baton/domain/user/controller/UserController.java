package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.dto.request.UserSignupReq;
import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.service.UserService;
import com.back.baton.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserSignupRes>> signup(@Valid @RequestBody UserSignupReq req){

        UserSignupRes res = userService.signup(req.email(), req.password(), req.nickname(), req.introduction(),req.profileImageUrl());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

}
