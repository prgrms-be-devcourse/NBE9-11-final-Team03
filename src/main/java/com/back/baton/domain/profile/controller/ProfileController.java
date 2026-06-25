package com.back.baton.domain.profile.controller;

import com.back.baton.domain.profile.dto.request.ProfileUpdateReq;
import com.back.baton.domain.profile.dto.response.MyProfileDetailRes;
import com.back.baton.domain.profile.dto.response.ProfileUpdateRes;
import com.back.baton.domain.profile.service.ProfileService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profile", description = "프로필 생성/수정 및 조회 API")
public class ProfileController {
    private final ProfileService profileService;

    @PatchMapping
    @Operation(
            summary = "프로필 수정",
            description = "현재 로그인한 작성자가 프로필의 한줄소개, 프로필이미지, 가진/원하는 재능 카테고리, 포트폴리오 링크를 원하는것만 골라 수정합니다."
    )
    public ResponseEntity<ApiResponse<ProfileUpdateRes>> updateProfile(
            @RequestBody @Valid ProfileUpdateReq req,
            @CurrentUser SecurityUser currentUser
    ){
        ProfileUpdateRes res = profileService.updateProfile(
                currentUser.getUserId(),
                req
        );
        return ApiResponses.success(SuccessCode.PROFILE_UPDATE_SUCCESS, res);
    }

    @GetMapping("/me")
    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 작성자가 자신의 프로필의 한줄소개, 프로필이미지, 가진/원하는 재능 카테고리, 포트폴리오 링크를 조회합니다."
    )
    public ResponseEntity<ApiResponse<MyProfileDetailRes>> getMyProfile(
            @CurrentUser SecurityUser currentUser
    ){
        MyProfileDetailRes res = profileService.getMyProfile(currentUser.getUserId());
        return ApiResponses.success(SuccessCode.PROFILE_FOUND_SUCCESS, res);
    }
}
