package com.back.baton.domain.user.service;

import com.back.baton.domain.user.dto.EmailVerification;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    @Value("${auth.email-verification.expiry-minutes:5}")
    private long expiryMinutes;

    private final EmailSender emailSender;

    private final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, EmailVerification> verifications = new ConcurrentHashMap<>();

    public void sendVerificationCode(String email) {
        email = normalize(email);
        EmailVerification verification = createVerification(false);
        // SMTP 이메일 인증 연결
        emailSender.sendVerificationCode(email, verification.code(), expiryMinutes);
        verifications.put(email,verification);
    }

    public void verifyEmail(String email, String code) { // 이메일 인증 처리(verified로 저장)
        email = normalize(email);
        EmailVerification verification = getExistingVerification(email);

        if (verification.expiredAt().isBefore(LocalDateTime.now())) {
            verifications.remove(email);
            throw new CustomException(UserErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }
        if (!verification.code().equals(code)) {
            throw new CustomException(UserErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }
        verifications.put(email, verification.markVerified());
    }

    public void consumeVerifiedEmail(String email){ // 인증 여부 확인 및 Verification 삭제
        email = normalize(email);
        EmailVerification verification = getExistingVerification(email);

        if(!verification.verified()){
            throw new CustomException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }
        verifications.remove(email);
    }

    public void markVerifiedForTrustedEmail(String email) { // BaseInitData 생성 위한 함수, 이메일 인증 완료된 객체 생성
        email = normalize(email);
        verifications.put(email, createVerification(true));
    }

    private EmailVerification getExistingVerification(String email){  // 유효한 verification인지 확인
        EmailVerification verification = verifications.get(email);
        if (verification == null) {
            throw new CustomException(UserErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
        }
        return verification;
    }

    private EmailVerification createVerification(boolean verified){ // 인증 객체 생성
        String code = generateCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        return new EmailVerification(code, expiredAt, verified);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String normalize(String email){
        return email.strip().toLowerCase();
    }
}
