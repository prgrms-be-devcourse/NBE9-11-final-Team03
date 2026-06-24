package com.back.baton.domain.user.service;

import com.back.baton.domain.user.dto.EmailVerification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    @Value("${auth.email-verification.expiry-minutes:5}")
    private long expiryMinutes;

    private final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, EmailVerification> verifications = new ConcurrentHashMap<>();

    public void sendVerificationCode(String email) {
        String code = generateCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        verifications.put(email, new EmailVerification(code, expiredAt, false));
        // TODO: JavaMailSender로 실제 메일 발송 연결
    }

    private String generateCode() { // 인증코드 6자리 생성
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
