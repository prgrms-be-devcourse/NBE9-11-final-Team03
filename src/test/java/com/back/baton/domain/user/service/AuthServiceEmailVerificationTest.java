package com.back.baton.domain.user.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.profile.service.ProfileService;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.RefreshTokenRepository;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.domain.user.repository.WithdrawnUserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceEmailVerificationTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordValidator passwordValidator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private CreditService creditService;
    @Mock private WithdrawnUserRepository withdrawnUserRepository;
    @Mock private WithdrawnEncoder withdrawnEncoder;
    @Mock private ProfileService profileService;
    @Mock private EmailVerificationService emailVerificationService;

    @Test
    @DisplayName("signup consumes verified email before saving user")
    void signup_success_consumesVerifiedEmail() {
        // given
        String email = "baton@domain.com";
        ReflectionTestUtils.setField(authService, "initialTrustScore", new BigDecimal("50.00"));
        given(passwordValidator.validate(eq("securePassword123!"), eq("baton"))).willReturn(true);
        given(passwordEncoder.encode("securePassword123!")).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        authService.signup(email, " securePassword123! ", "batonNick", "hello baton", null);

        // then
        verify(emailVerificationService).consumeVerifiedEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("signup fails before saving user when email is not verified")
    void signup_fail_whenEmailIsNotVerified() {
        // given
        String email = "baton@domain.com";
        doThrow(new CustomException(UserErrorCode.EMAIL_NOT_VERIFIED))
                .when(emailVerificationService).consumeVerifiedEmail(email);

        // when & then
        assertThatThrownBy(() -> authService.signup(email, "securePassword123!", "batonNick", "hello baton", null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.EMAIL_NOT_VERIFIED);

        verify(emailVerificationService).consumeVerifiedEmail(email);
        verify(passwordValidator, never()).validate(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(creditService);
        verifyNoInteractions(profileService);
    }

    @Test
    @DisplayName("send email verification code normalizes email and delegates")
    void sendEmailVerificationCode_success() {
        // when
        authService.sendEmailVerificationCode("Baton@Domain.com");

        // then
        verify(userRepository).existsByEmail("baton@domain.com");
        verify(withdrawnUserRepository).existsByEncodedEmail(withdrawnEncoder.encode("baton@domain.com"));
        verify(emailVerificationService).sendVerificationCode("baton@domain.com");
    }

    @Test
    @DisplayName("verify email delegates to email verification service")
    void verifyEmail_success() {
        // when
        authService.verifyEmail("baton@domain.com", "123456");

        // then
        verify(emailVerificationService).verifyEmail("baton@domain.com", "123456");
    }
}
