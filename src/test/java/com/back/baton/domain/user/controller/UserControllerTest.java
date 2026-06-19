package com.back.baton.domain.user.controller;

import com.back.baton.domain.user.dto.request.UserLoginReq;
import com.back.baton.domain.user.dto.request.UserSignupReq;
import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.dto.response.UserTokenDto;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.service.UserService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.response.code.TokenErrorCode;
import com.back.baton.global.response.code.UserErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean // 또는 @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입 성공 - 유효한 DTO 요청 시 201 CREATED를 반환한다")
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
                .profileImageUrl("https://image.com/profile.png")
                .nickname("바톤닉네임")
                .introduction("안녕하세요 바톤입니다.")
                .trustScore(new BigDecimal(50.00))
                .build();

        given(userService.signup(anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(new UserSignupRes(mockUser));
        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.data.email").value("baton@domain.com"))
                .andExpect(jsonPath("$.data.nickname").value("바톤닉네임"))
                .andExpect(jsonPath("$.data.introduction").value("안녕하세요 바톤입니다."))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated());
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
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API 로그인 성공 - AccessToken은 Body에 담기고, RefreshToken은 HttpOnly 보안 쿠키로 헤더에 설정된다")
    void login_Success_1() throws Exception {
        // given
        String path = "/api/v1/auth/login";
        UserLoginReq requestDto = new UserLoginReq("test@example.com", "rawPassword123");

        // UserService의 login 결과를 가짜(Mock)로 준비
        UserTokenDto mockTokenOutput = new UserTokenDto("mock_access_token_value", "mock_refresh_token_value");
        given(userService.login(eq("test@example.com"), eq("rawPassword123"))).willReturn(mockTokenOutput);

        // when & then
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())

                // 1. 응답 바디 JSON에 Access Token이 표준 규격에 맞게 매핑되어 내려오는지 확인
                .andExpect(jsonPath("$.data.accessToken").value("mock_access_token_value"))

                // 2. 응답 헤더에 Set-Cookie가 작동하며, 우리가 설계한 보안 옵션들이 정상 부착되었는지 정밀 검증
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("refreshToken=mock_refresh_token_value")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("SameSite=Strict")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Path=/")));
    }

    @Test
    @DisplayName("API 로그인 실패 - 비즈니스 로직에서 에러(CustomException)가 터지면 컨트롤러는 그대로 에러를 상위로 던진다")
    void login_Fail_1() throws Exception {
        // given
        String path = "/api/v1/auth/login";
        UserLoginReq requestDto = new UserLoginReq("test@example.com", "wrongPassword");

        // 의도적으로 서비스에서 예외가 터지도록 Mocking 환경 세팅
        given(userService.login(anyString(), anyString()))
                .willThrow(new CustomException(UserErrorCode.INVALID_PASSWORD));

        // when & then
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // 프로젝트에 GlobalExceptionHandler가 설정되어 있다면 status().isBadRequest() 등으로 세부 핸들링 검증도 가능합니다.
                .andExpect(status().is4xxClientError());
    }
    @Test
    @DisplayName("토큰 재발급 성공: 쿠키에 refreshToken이 있으면 재발급 후 200 반환 및 새 쿠키 설정")
    void reissue_Success() throws Exception {
        // given
        String originRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        UserTokenDto mockDto = new UserTokenDto(newAccessToken, newRefreshToken);
        given(userService.reissue(originRefreshToken)).willReturn(mockDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/reissue")
                        .cookie(new Cookie("refreshToken", originRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.USER_REISSUE_SUCCESS.getCode())) // 프로젝트 구조에 맞게 검증
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                // Set-Cookie 헤더 검증 (HttpOnly, Secure, SameSite 등 포함 여부)
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=" + newRefreshToken)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("SameSite=Strict")));
    }

    @Test
    @DisplayName("토큰 재발급 실패: 쿠키에 refreshToken이 없는 경우 서비스에서 예외를 던지고 에러 응답을 반환한다")
    void reissue_Fail_WhenCookieIsEmpty() throws Exception {
        // given
        // 컨트롤러에서 required = false이므로 null이 서비스로 넘어감 -> 서비스에서 TOKEN_NOT_FOUND 던짐 가정
        given(userService.reissue(null)).willThrow(new CustomException(TokenErrorCode.TOKEN_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/v1/auth/reissue")) // 쿠키 없이 요청
                .andExpect(status().isNotFound()) // 예외 핸들러가 반환하는 HttpStatus에 맞게 설정 (ex: isBadRequest or isUnauthorized)
                .andExpect(jsonPath("$.code").value(TokenErrorCode.TOKEN_NOT_FOUND.getCode()));
    }
}