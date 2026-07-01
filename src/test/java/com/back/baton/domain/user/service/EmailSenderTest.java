package com.back.baton.domain.user.service;

import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailSender emailSender;

    @Test
    @DisplayName("메일 발송 실패 시 원인 예외(SMTP 등)가 CustomException.getCause()에 보존된다")
    void sendVerificationCode_preservesCause_onFailure() {
        // given
        MailSendException smtpError = new MailSendException("SMTP 연결 실패");
        doThrow(smtpError).when(mailSender).send(any(SimpleMailMessage.class));

        // when & then
        assertThatThrownBy(() -> emailSender.sendVerificationCode("to@test.com", "123456", 5))
                .isInstanceOf(CustomException.class)
                .satisfies(thrown -> {
                    CustomException ce = (CustomException) thrown;
                    assertThat(ce.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_SEND_FAILED);
                    assertThat(ce.getCause()).isSameAs(smtpError);
                });
    }
}
