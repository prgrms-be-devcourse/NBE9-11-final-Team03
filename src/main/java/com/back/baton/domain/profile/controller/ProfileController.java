package com.back.baton.domain.profile.controller;

import com.back.baton.domain.profile.dto.requset.ProfileUpdateReq;
import com.back.baton.domain.profile.dto.response.ProfileUpdateRes;
import com.back.baton.domain.profile.service.ProfileService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {
    private final ProfileService profileService;

    @PatchMapping
    public ResponseEntity<ApiResponse<ProfileUpdateRes>> updateProfile(
            @RequestBody @Valid ProfileUpdateReq req,
            @CurrentUser SecurityUser currentUser
    ){
        ProfileUpdateRes res = profileService.updateProfile(currentUser.getUserId(),
                req.profileImageUrl(), req.introduction(),
                req.myTalentCategoryIds(), req.wantTalentCategoryIds(), req.portfolioLinkList());
        return ApiResponses.success(SuccessCode.PROFILE_UPDATE_SUCCESS, res);
    }
}
