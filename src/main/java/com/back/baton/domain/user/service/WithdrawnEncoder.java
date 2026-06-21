package com.back.baton.domain.user.service;

import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class WithdrawnEncoder {
    @Value("${hash.salt}")
    private String salt;

    public String encode(String plainText) {
        if(plainText ==null || plainText.isBlank()){
                throw new CustomException(UserErrorCode.INVALID_EMAIL);
        }
        try {
            plainText = plainText.strip().toLowerCase();
            plainText += salt; // 레인보우 테이블 방지

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash); // 16진수 문자열로 변환

        } catch (Exception e) {
            throw new CustomException(UserErrorCode.INVALID_EMAIL);
        }
    }
}
