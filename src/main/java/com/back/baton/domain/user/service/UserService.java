package com.back.baton.domain.user.service;

import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;

    @Value("${user.initial-trust-score}")
    private BigDecimal initialTrustScore; // 초기 신뢰 점수

    public UserSignupRes signup(String email, String password, String nickname, String introduction, String profileImgUrl) {
        // 1. 이메일 검증
        if(userRepository.existsByEmail(email)){
            throw new CustomException(UserErrorCode.DUPLICATED_USER);
        }

        // TODO: 1-2. 이메일 인증 여부 확인

        // 2. 닉네임 검증
        LocalDateTime defaultDeletedAt = LocalDateTime.of(1880, 6, 16,0,0,0); // 과거의 시점으로 고정

        if(userRepository.existsByNicknameAndDeletedAt(nickname, defaultDeletedAt)){
            throw new CustomException(UserErrorCode.DUPLICATED_USER);
        }
        //TODO: 2-2. 닉네임 중복 확인 별도 구현

        // 3. 비밀번호 형식 검증
        password = password.strip(); // 앞뒤 공백 제거
        int lastAtIndex = email.lastIndexOf('@');
        String username = email.substring(0, lastAtIndex); // 이메일 앞자리 추출
        if(!passwordValidator.validate(password, username)){ // 비밀번호 검증
            throw new CustomException(UserErrorCode.INVALID_PASSWORD);
        }

        // 4. 비밀번호 암호화-> 솔트 + 암호화 + 연산 반복 -> BCrypt 적용
        String encodedPwd = passwordEncoder.encode(password);

        // 5. user 생성
        User user = User.builder()
                .email(email)
                .password(encodedPwd)
                .nickname(nickname)
                .profileImageUrl(profileImgUrl)
                .introduction(introduction)
                .trustScore(initialTrustScore)
                .build();
        userRepository.save(user);

        // TODO: 6. Account 생성

        return new UserSignupRes(user);
    }


}
