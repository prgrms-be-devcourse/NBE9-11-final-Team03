package com.back.baton.domain.user.service;

import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

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
        ReflectionTestUtils.setField(userService, "initialTrustScore", new BigDecimal("50.00"));

        given(passwordValidator.validate(eq("securePassword123!"), eq("baton")))
                .willReturn(true);
        given(passwordEncoder.encode("securePassword123!"))
                .willReturn("$2a$10$encodedHashText");
        given(userRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserSignupRes response = userService.signup(email, password, nickname, introduction, profileImgUrl);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.nickname()).isEqualTo(nickname);

        // 내부 로직이 정확한 인자로 호출되었는지 대조 검증
        verify(passwordValidator).validate("securePassword123!", "baton");
        verify(passwordEncoder).encode("securePassword123!");
        verify(userRepository).save(any(User.class));
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
        assertThatThrownBy(() -> userService.signup(email, password, nickname, introduction, profileImgUrl))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode") // 에러코드 필드 검증 (본인 CustomException 구조에 맞게 변경 가능)
                .isEqualTo(UserErrorCode.INVALID_PASSWORD);

        // 패스워드가 틀렸으므로 암호화 및 저장 로직은 절대 실행되면 안 됨
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
