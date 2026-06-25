package com.back.baton.domain.user.service;

import com.back.baton.domain.user.dto.EmailVerification;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailVerificationServiceTest {

    private EmailSender emailSender;
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        emailSender = mock(EmailSender.class);
        emailVerificationService = new EmailVerificationService(emailSender);
        ReflectionTestUtils.setField(emailVerificationService, "expiryMinutes", 5L);
    }

    @Test
    @DisplayName("인증 코드 발송 시 메일을 발송하고 미인증 상태의 인증 요청을 저장한다")
    void sendVerificationCode_storesUnverifiedCode() {
        // given
        String email = "user@example.com";

        // when
        emailVerificationService.sendVerificationCode(email);

        // then
        EmailVerification verification = getVerification(email);
        assertThat(verification).isNotNull();
        assertThat(verification.code()).matches("\\d{6}");
        assertThat(verification.expiredAt()).isAfter(LocalDateTime.now());
        assertThat(verification.verified()).isFalse();
        verify(emailSender).sendVerificationCode(eq(email), matches("\\d{6}"), eq(5L));
    }

    @Test
    @DisplayName("정상 인증 코드로 이메일 인증을 완료하고 회원가입 시 인증 정보를 소비한다")
    void verifyEmail_andConsumeVerifiedEmail_success() {
        // given
        String email = "user@example.com";
        emailVerificationService.sendVerificationCode(email);
        String code = getVerification(email).code();

        // when
        emailVerificationService.verifyEmail(email, code);

        // then
        assertThat(getVerification(email).verified()).isTrue();
        assertThatCode(() -> emailVerificationService.consumeVerifiedEmail(email))
                .doesNotThrowAnyException();
        assertThat(getVerification(email)).isNull();
    }

    @Test
    @DisplayName("이메일 인증은 대소문자가 달라도 같은 이메일로 처리한다")
    void verifyEmail_normalizesEmail() {
        // given
        emailVerificationService.sendVerificationCode("User@Example.com");
        String code = getVerification("user@example.com").code();

        // when
        emailVerificationService.verifyEmail("USER@example.com", code);

        // then
        assertThat(getVerification("user@example.com").verified()).isTrue();
        verify(emailSender).sendVerificationCode(eq("user@example.com"), matches("\\d{6}"), eq(5L));
    }

    @Test
    @DisplayName("인증 요청이 없는 이메일을 검증하면 NOT_FOUND 예외가 발생한다")
    void verifyEmail_failWhenRequestNotFound() {
        assertThatThrownBy(() -> emailVerificationService.verifyEmail("missing@example.com", "123456"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("인증 코드가 다르면 INVALID_EMAIL_VERIFICATION_CODE 예외가 발생한다")
    void verifyEmail_failWhenCodeDoesNotMatch() {
        // given
        String email = "user@example.com";
        emailVerificationService.sendVerificationCode(email);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmail(email, "000000"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
    }

    @Test
    @DisplayName("만료된 인증 코드는 EXPIRED 예외를 발생시키고 저장소에서 제거된다")
    void verifyEmail_failWhenCodeExpired() {
        // given
        String email = "user@example.com";
        getVerifications().put(email, new EmailVerification("123456", LocalDateTime.now().minusSeconds(1), false));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmail(email, "123456"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.EMAIL_VERIFICATION_EXPIRED);
        assertThat(getVerification(email)).isNull();
    }

    @Test
    @DisplayName("인증 완료 전 회원가입 소비를 시도하면 EMAIL_NOT_VERIFIED 예외가 발생한다")
    void consumeVerifiedEmail_failWhenNotVerified() {
        // given
        String email = "user@example.com";
        emailVerificationService.sendVerificationCode(email);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.consumeVerifiedEmail(email))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    @DisplayName("초기 데이터용 인증 완료 처리는 회원가입 소비 검증을 통과한다")
    void markVerifiedForTrustedEmail_success() {
        // given
        String email = "seed@example.com";

        // when
        emailVerificationService.markVerifiedForTrustedEmail(email);

        // then
        assertThat(getVerification(email).verified()).isTrue();
        assertThatCode(() -> emailVerificationService.consumeVerifiedEmail(email))
                .doesNotThrowAnyException();
        assertThat(getVerification(email)).isNull();
    }

    @SuppressWarnings("unchecked")
    private Map<String, EmailVerification> getVerifications() {
        return (Map<String, EmailVerification>) ReflectionTestUtils.getField(emailVerificationService, "verifications");
    }

    private EmailVerification getVerification(String email) {
        return getVerifications().get(email);
    }
}
