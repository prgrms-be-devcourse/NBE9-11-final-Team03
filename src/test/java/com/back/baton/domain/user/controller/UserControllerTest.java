package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.dto.request.UserSignupReq;
import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공 - 유효한 DTO 요청 시 200 OK를 반환한다")
    void create_1() throws Exception {
        // given
        UserSignupReq request = new UserSignupReq(
                "baton@domain.com",
                "securePassword123!",
                "바톤닉네임",
                "https://image.com/profile.png",
                "안녕하세요 바톤입니다."
        );

        // 가짜 가입 유저 생성 (응답용)
        User mockUser = User.builder()
                .email("baton@domain.com")
                .password("encodedPassword")
                .nickname("바톤닉네임")
                .build();

        given(userService.signup(anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(new UserSignupRes(mockUser));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 실패 - DTO 애노테이션 검증 조건 위반 시 400 Bad Request를 반환한다")
    void create_2() throws Exception {
        // given
        UserSignupReq invalidRequest = new UserSignupReq(
                "wrong-email-format", // @Email 위반
                "short",               // @Size(min=8) 위반
                "닉",                  // @Size(min=3) 위반
                "https://image.com/profile.png",
                "소개"                 // @Size(min=5) 위반
        );

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        resultActions.andExpect(status().isBadRequest());
    }
}