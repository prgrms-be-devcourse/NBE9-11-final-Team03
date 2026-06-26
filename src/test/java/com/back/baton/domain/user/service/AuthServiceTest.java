package com.back.baton.domain.user.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.profile.service.ProfileService;
import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.dto.response.UserTokenDto;
import com.back.baton.domain.user.entity.RefreshToken;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.repository.RefreshTokenRepository;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.domain.user.repository.WithdrawnUserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TokenErrorCode;
import com.back.baton.global.response.code.UserErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordValidator passwordValidator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private WithdrawnEncoder withdrawnEncoder;
    @Mock private WithdrawnUserRepository withdrawnUserRepository;
    @Mock private CreditService creditService;
    @Mock private ProfileService profileService;
    @Mock private EmailVerificationService emailVerificationService;
    private User validUser;
    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("nickname")
                .introduction("introduction")
                .build();

    }
    @Test
    @DisplayName("회원가입 성공 - 비밀번호 검증을 통과하고 암호화되어 정상 저장된다")
    void create_1() {
        // given
        String email = "baton@domain.com";
        String password = " securePassword123! "; // 앞뒤 공백을 일부러 추가
        String nickname = "바톤닉네임";
        String introduction = "안녕하세요 바톤입니다.";
        String profileImgUrl = "https://image.com/profile.png";

        // strip()이 적용된 패스워드와 추출된 username("baton") 검증이 통과한다고 가정
        ReflectionTestUtils.setField(authService, "initialTrustScore", new BigDecimal("50.00"));

        given(passwordValidator.validate(eq("securePassword123!"), eq("baton")))
                .willReturn(true);
        given(passwordEncoder.encode("securePassword123!"))
                .willReturn("$2a$10$encodedHashText");
        given(userRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserSignupRes response = authService.signup(email, password, nickname, introduction, profileImgUrl);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.nickname()).isEqualTo(nickname);

        // 내부 로직이 정확한 인자로 호출되었는지 대조 검증
        verify(withdrawnUserRepository).existsByEncodedEmail(withdrawnEncoder.encode(email));
        verify(emailVerificationService).consumeVerifiedEmail(email);
        verify(passwordValidator).validate("securePassword123!", "baton");
        verify(passwordEncoder).encode("securePassword123!");
        verify(userRepository).save(any(User.class));
        verify(creditService).initializeAccount(response.id());
    }

    @Test
    @DisplayName("회원가입 실패 - PasswordValidator 조건 불충족 시 CustomException이 발생한다")
    void create_2() {
        // given
        String email = "baton@domain.com";
        String password = "simplepassword";
        String nickname = "바톤닉네임";
        String introduction = "안녕하세요 바톤입니다.";
        String profileImgUrl = "https://image.com/profile.png";

        // 패스워드 검증기에서 거부(false) 반환 설정
        given(passwordValidator.validate(eq("simplepassword"), eq("baton")))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.signup(email, password, nickname, introduction, profileImgUrl))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode") // 에러코드 필드 검증 (본인 CustomException 구조에 맞게 변경 가능)
                .isEqualTo(UserErrorCode.INVALID_PASSWORD_FORMAT);

        // 패스워드가 틀렸으므로 암호화 및 저장 로직은 절대 실행되면 안 됨
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    @DisplayName("로그인 성공 - 기존 RefreshToken이 없는 경우 새로운 토큰을 생성(save)한다")
    void login_success_1() {
        // given
        String email = "test@example.com";
        String password = "rawPassword "; // 공백 포함

        given(userRepository.findByEmail(email)).willReturn(Optional.of(validUser));
        given(passwordEncoder.matches("rawPassword", validUser.getPassword())).willReturn(true); // strip 처리 검증

        // 발급 시간 매칭을 위해 any(Date.class) 사용
        given(jwtTokenProvider.createAccessToken(eq(validUser.getId()), eq("USER"), any(Date.class))).willReturn("access_token_value");
        given(jwtTokenProvider.createRefreshToken(eq(validUser.getId()), any(Date.class))).willReturn("refresh_token_value");

        // 기존 토큰이 비어있는 상태 시뮬레이션 (Optional.empty)
        given(refreshTokenRepository.findByUserId(validUser.getId())).willReturn(Optional.empty());

        // when
        UserTokenDto result = authService.login(email, password);

        // then
        assertNotNull(result);
        assertEquals("access_token_value", result.accessToken());
        assertEquals("refresh_token_value", result.refreshToken());

        // 새로운 RefreshToken 객체가 save() 되었는지 검증
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 성공 - 기존 RefreshToken이 이미 존재하는 경우 값을 갱신(update)한다")
    void login_Success_2() {
        // given
        String email = "test@example.com";
        String password = "rawPassword";

        RefreshToken existingToken = spy(new RefreshToken(1L, "old_token", LocalDateTime.now()));

        given(userRepository.findByEmail(email)).willReturn(Optional.of(validUser));
        given(passwordEncoder.matches(password, validUser.getPassword())).willReturn(true);
        given(jwtTokenProvider.createAccessToken(eq(validUser.getId()), eq("USER"), any(Date.class))).willReturn("new_access_token");
        given(jwtTokenProvider.createRefreshToken(eq(validUser.getId()), any(Date.class))).willReturn("new_refresh_token");

        // 기존 토큰이 이미 디비에 존재하는 상태 시뮬레이션
        given(refreshTokenRepository.findByUserId(validUser.getId())).willReturn(Optional.of(existingToken));

        // when
        UserTokenDto result = authService.login(email, password);

        // then
        assertNotNull(result);
        assertEquals("new_access_token", result.accessToken());
        assertEquals("new_refresh_token", result.refreshToken());

        // updateToken() 메서드가 정상 호출되어 내부 값이 바뀌었는지 검증
        verify(existingToken, times(1)).update(eq("new_refresh_token"), any(LocalDateTime.class));
        // 새 토큰이 아니므로 대전제에 따라 save()는 호출되지 않아야 함
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 유저 상태가 휴면(SUSPENDED)인 경우 예외가 발생한다")
    void login_Fail_1() {
        // given
        validUser.changeStatus(UserStatus.SUSPENDED);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(validUser));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.login("test@example.com", "rawPassword");
        });
        assertEquals(UserErrorCode.SUSPENDED_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호가 일치하지 않는 경우 INVALID_PASSWORD 예외가 발생한다")
    void login_Fail_2() {
        // given
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(validUser));
        given(passwordEncoder.matches(anyString(), eq(validUser.getPassword()))).willReturn(false);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.login("test@example.com", "wrongPassword");
        });
        assertEquals(UserErrorCode.INVALID_PASSWORD, exception.getErrorCode());
    }
    @Test
    @DisplayName("토큰 재발급 성공: 요청 토큰이 DB의 최신 토큰과 일치하면 새 토큰 세트를 반환하고 DB를 갱신한다")
    void reissue_Success() {
        // given
        String savedToken = "valid-refresh-token";
        Long userId = validUser.getId();
        RefreshToken mockRefreshToken = spy(new RefreshToken(userId, savedToken, LocalDateTime.now().plusDays(14)));
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        given(jwtTokenProvider.getUserIdFromToken(savedToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(validUser)); // @BeforeEach의 mockUser 사용
        given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(mockRefreshToken));

        given(jwtTokenProvider.createAccessToken(eq(userId), eq("USER"), any(Date.class))).willReturn(newAccessToken);
        given(jwtTokenProvider.createRefreshToken(eq(userId), any(Date.class))).willReturn(newRefreshToken);

        // when
        UserTokenDto result = authService.reissue(savedToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.refreshToken()).isEqualTo(newRefreshToken);

        verify(mockRefreshToken).update(eq(newRefreshToken), any(LocalDateTime.class));
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    // --- [ 실패 케이스 ] ---

    @Test
    @DisplayName("토큰 재발급 실패: 파라미터가 Null인 경우 TOKEN_NOT_FOUND 예외가 발생한다")
    void reissue_Fail_WhenTokenIsNull() {
        // given
        String nullToken = null;

        // when & then
        assertThatThrownBy(() -> authService.reissue(nullToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TokenErrorCode.TOKEN_NOT_FOUND);

        verifyNoInteractions(jwtTokenProvider);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("토큰 재발급 실패: 유저가 DB에 존재하지 않는 경우 USER_NOT_FOUND 예외가 발생한다")
    void reissue_Fail_WhenUserNotFound() {
        // given
        String savedToken = "valid-refresh-token";
        Long userId = validUser.getId();
        given(jwtTokenProvider.getUserIdFromToken(savedToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.empty()); // 유저가 없는 상황 Mocking

        // when & then
        assertThatThrownBy(() -> authService.reissue(savedToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

        verifyNoInteractions(refreshTokenRepository);
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = {"SUSPENDED", "DORMANT", "WITHDRAWN"})
    @DisplayName("토큰 재발급 실패: 유저가 활성화(ACTIVE) 상태가 아니면 상태에 맞는 예외가 발생한다")
    void reissue_Fail_WhenUserStatusIsNotActive(UserStatus status) {
        // given
        // 테스트 메서드 안에서 @BeforeEach로 생성된 mockUser의 상태를 파라미터 값으로 변경합니다.
        String savedToken = "valid-refresh-token";
        Long userId = validUser.getId();
        validUser.changeStatus(status);

        given(jwtTokenProvider.getUserIdFromToken(savedToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(validUser));

        UserErrorCode expectedErrorCode = switch (status) {
            case SUSPENDED -> UserErrorCode.SUSPENDED_STATUS;
            case DORMANT -> UserErrorCode.DORMANT_STATUS;
            case WITHDRAWN -> UserErrorCode.WITHDRAWN_STATUS;
            default -> null;
        };

        // when & then
        assertThatThrownBy(() -> authService.reissue(savedToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", expectedErrorCode);

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("토큰 재발급 실패: 유저 ID로 저장된 리프레시 토큰 데이터가 없으면 TOKEN_NOT_FOUND 예외가 발생한다")
    void reissue_Fail_WhenRefreshTokenNotFoundInDb() {
        // given
        String savedToken = "valid-refresh-token";
        Long userId = validUser.getId();
        given(jwtTokenProvider.getUserIdFromToken(savedToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(validUser));
        given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.empty()); // DB에 토큰 없음

        // when & then
        assertThatThrownBy(() -> authService.reissue(savedToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TokenErrorCode.TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("★토큰 탈취(RTR 자폭) 검증: 요청된 토큰이 DB의 최신 토큰 값과 다르면, 해당 토큰을 즉시 삭제하고 REUSED_REFRESH_TOKEN 예외를 던진다")
    void reissue_Fail_And_KillSwitch_WhenTokenValueMismatched() {
        // given
        String stolenToken = "stolen-old-refresh-token"; // 해커가 찌른 옛날 토큰
        String latestDbToken = "latest-db-refresh-token"; // DB에 덮어써져 있는 최신 토큰
        Long userId = validUser.getId();

        RefreshToken mockRefreshToken = new RefreshToken(userId, latestDbToken, LocalDateTime.now().plusDays(14));

        // stolenToken으로 찔렀을 때의 연쇄 흐름 Mocking
        given(jwtTokenProvider.getUserIdFromToken(stolenToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(validUser));
        given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(mockRefreshToken));

        // when & then
        assertThatThrownBy(() -> authService.reissue(stolenToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TokenErrorCode.REUSED_TOKEN);

        // 자폭(Delete) 쿼리가 나갔는지 핵심 검증
        verify(refreshTokenRepository).delete(mockRefreshToken);
        verify(jwtTokenProvider, never()).createAccessToken(anyLong(), anyString(), any(Date.class));
    }
    @Test
    @DisplayName("로그아웃 시 해당 유저 ID의 리프레시 토큰 벌크 삭제 메서드를 호출한다")
    void logout_service_success() {
        // given
        Long userId = 1L;

        // when
        authService.logout(userId);

        // then
        // 우리가 리팩토링했던 @Modifying 벌크 삭제 쿼리 메서드가 단 1회 정상 호출되었는지 검증
        verify(refreshTokenRepository, times(1)).deleteByUserIdCustom(userId);
    }
}
