package com.back.baton.domain.user.service;

import com.back.baton.domain.user.dto.EmailVerification;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@Slf4j
public class EmailVerificationService {

    @Value("${auth.email-verification.expiry-minutes:5}")
    private long expiryMinutes;

    private final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, EmailVerification> verifications = new ConcurrentHashMap<>();

    // 이메일 인증 코드 발송
    public void sendVerificationCode(String email) {
        String code = generateCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        verifications.put(email, new EmailVerification(code, expiredAt, false));
        log.info("code"+code);
        // TODO: JavaMailSender로 실제 메일 발송 연결
    }

    // 인증 처리
    public void verifyEmail(String email, String code){
        EmailVerification verification = verifications.get(email);
        // 이메일에 대한 인증 코드가 저장되지 않은 경우
        if(verification == null){
            throw new CustomException(UserErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
        }
        // 만료된 인증 코드인 경우
        if(verification.expiredAt().isBefore(LocalDateTime.now())) {
            verifications.remove(email);
            throw new CustomException(UserErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }
        // 인증 코드가 다른 경우
        if(!verification.code().equals(code)){
            throw new CustomException(UserErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }
        verifications.put(email, verification.markVerified()); // 인증 완료 표기
    }

    // baseInitData 세팅용 함수 - 코드 생성 & 인증됨으로 저장
    public void markVerifiedForInitData(String email) {
        String code = generateCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        verifications.put(email, new EmailVerification(code, expiredAt, true));
    }

    // 회원가입할 때 이메일 인증 여부 검증, 지우기
    public void consumeVerifiedEmail(String email){
        EmailVerification verification = verifications.get(email);
        if(verification == null){
            throw new CustomException(UserErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
        }
        if(!verification.verified()){
            throw new CustomException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }
        verifications.remove(email);
    }

    // 인증코드 6자리 생성
    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
