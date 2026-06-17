package com.back.baton.domain.user.service;

import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.dto.response.UserTokenDto;
import com.back.baton.domain.user.entity.RefreshToken;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.repository.RefreshTokenRepository;
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
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

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
            throw new CustomException(UserErrorCode.INVALID_PASSWORD_FORMAT);
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

    public UserTokenDto login(String email, String password) {
        // 1. User 검증
        User user = userRepository.findByEmail(email).orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));
        if(user.getStatus().equals(UserStatus.SUSPENDED)){ // 휴면
            throw new CustomException(UserErrorCode.SUSPENDED_STATUS);
        }
        if(user.getStatus().equals(UserStatus.DORMANT)){ // 정지
            throw new CustomException(UserErrorCode.DORMANT_STATUS);
        }
        if(user.getStatus().equals(UserStatus.WITHDRAWN)){ // 탈퇴
            throw new CustomException(UserErrorCode.WITHDRAWN_STATUS);
        }

        // 2. 비밀번호 검증
        password = password.strip();
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new CustomException(UserErrorCode.INVALID_PASSWORD);
        }

        // 3. 토큰 발행
        Date now = new Date(); // 발급 시간 고정
        String accessTokenValue = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().toString(), now);
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId(), now);

        // 4. refreshToken RefreshToken Table에 저장
        Date expiration = new Date (now.getTime() + 14 * 24 * 60 * 60 * 1000L); //refresh 만료 시간 계산
        LocalDateTime expiredAt = LocalDateTime.ofInstant( //type 변환
                expiration.toInstant(),
                ZoneId.systemDefault()
        );
        refreshTokenRepository.findByUserId(user.getId()).ifPresentOrElse(
                // 이전에 만료된 토큰이 존재하는 경우(찌꺼기)
                existingToken -> existingToken.update(refreshTokenValue, expiredAt),
                // 없는 경우
                ()-> {
                    RefreshToken refreshToken = new RefreshToken(user.getId(), refreshTokenValue, expiredAt);
                    refreshTokenRepository.save(refreshToken);
                }
        );

        return new UserTokenDto(accessTokenValue, refreshTokenValue);
    }
}
