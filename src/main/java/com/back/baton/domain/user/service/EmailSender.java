package com.back.baton.domain.user.service;

import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendVerificationCode(String to, String code, long expiryMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("[BATON] 이메일 인증 코드");
        message.setText("""
                BATON 이메일 인증 코드입니다.

                인증 코드: %s
                유효 시간: %d분

                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code, expiryMinutes));
        try{
            mailSender.send(message);
        }catch (Exception e){
            // 원인 예외(SMTP 오류 등)를 함께 전달해 Sentry에 근본 원인이 남도록 한다.
            throw new CustomException(UserErrorCode.EMAIL_SEND_FAILED, e);
        }
    }
}