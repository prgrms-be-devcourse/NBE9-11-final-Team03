package com.back.baton.domain.profile.controller;

import com.back.baton.domain.profile.dto.request.ProfileUpdateReq;
import com.back.baton.domain.profile.dto.response.MyProfileDetailRes;
import com.back.baton.domain.profile.service.ProfileService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.response.code.UserErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.support.security.WithMockSecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ProfileService profileService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    private static final String BASE = "/api/v1/profiles";

    @Test
    @DisplayName("프로필 업데이트 성공 - 200")
    @WithMockSecurityUser(userId = 1)
    void updateProfile_success() throws Exception {
        // given: 카테고리는 실제 비즈니스 로직(Service)에서 맵핑되므로 컨트롤러 테스트는 DTO만 검증
        var req = new ProfileUpdateReq("https://image.png", "안녕하세요 개발자입니다.", List.of(1L), List.of(2L), List.of("https://git.com"));

        // 서비스 모킹 (Category.create()는 서비스 내부에서 쓰이므로 여기선 생략 가능)
        given(profileService.updateProfile(eq(1L), eq(req)))
                .willReturn(null);

        mockMvc.perform(patch(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("유저를 찾을 수 없는 경우 404/400 (로직에 따른 예외)")
    @WithMockSecurityUser(userId = 1)
    void updateProfile_user_not_found() throws Exception {
        // 1. 서비스 모킹: 파라미터 2개(Long, ProfileUpdateReq)에 맞게 수정
        willThrow(new CustomException(UserErrorCode.USER_NOT_FOUND))
                .given(profileService).updateProfile(eq(1L), any(ProfileUpdateReq.class));

        // 2. 요청 DTO 생성
        var req = new ProfileUpdateReq("url", "정상적인소개글입니다", null, null, null);

        // 3. 실행
        mockMvc.perform(patch(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("GET /api/profiles/me - 내 프로필 조회 성공")
    // @WithMockUser 또는 커스텀 시큐리티 어노테이션을 사용하여 SecurityUser 환경을 모킹해야 합니다.
    @WithMockSecurityUser(userId = 1L)
    void getMyProfile_Success() throws Exception {
        // given
        Long userId = 1L;
        MyProfileDetailRes responseDto = new MyProfileDetailRes(
                1L, "테스터", "https://image.com", "안녕하세요",
                BigDecimal.valueOf(99.9), List.of("https://github.com"),
                List.of(), List.of(), true
        );

        given(profileService.getMyProfile(userId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/profiles/me") // 실제 매핑된 URL 구조에 맞게 변경 필요
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.PROFILE_FOUND_SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.nickname").value("테스터"))
                .andExpect(jsonPath("$.data.visible").value(true))
                .andDo(print());
    }
}