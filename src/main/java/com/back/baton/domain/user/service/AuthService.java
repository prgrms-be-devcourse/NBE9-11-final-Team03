package com.back.baton.domain.user.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.profile.service.ProfileService;
import com.back.baton.domain.user.dto.response.UserSignupRes;
import com.back.baton.domain.user.dto.response.UserTokenDto;
import com.back.baton.domain.user.entity.RefreshToken;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.repository.RefreshTokenRepository;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.domain.user.repository.WithdrawnUserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TokenErrorCode;
import com.back.baton.global.response.code.UserErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CreditService creditService;
    private final WithdrawnUserRepository withdrawnUserRepository;
    private final WithdrawnEncoder withdrawnEncoder;
    private final ProfileService profileService;
    private final EmailVerificationService emailVerificationService;

    @Value("${user.initial-trust-score}")
    private BigDecimal initialTrustScore; // 초기 신뢰 점수

    public UserSignupRes signup(String email, String password, String nickname, String introduction, String profileImgUrl) {
        // 1. 이메일 검증
        email = normalizeEmail(email); // 이메일 형식 변환
        consumeNotDuplicatedEmail(email); // 이메일 중복 여부 확인
        consumeVerifiedEmail(email);  // 이메일 인증 여부 확인

        // 2. 닉네임 검증
        checkNicknameDuplicated(nickname);

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

        // 6. Account 생성
        creditService.initializeAccount(user.getId());

        //7. 기본 profile 생성
        profileService.initializeProfile(user);

        return new UserSignupRes(user);
    }

    public void checkNicknameDuplicated(String nickname){
        LocalDateTime defaultDeletedAt = LocalDateTime.of(1880, 6, 16,0,0,0); // 과거의 시점으로 고정

        if(userRepository.existsByNicknameAndDeletedAt(nickname, defaultDeletedAt)){
            throw new CustomException(UserErrorCode.DUPLICATED_USER);
        }
    }

    public void sendEmailVerificationCode(String email) { // 이메일 인증번호 보내기
        email = normalizeEmail(email);
        consumeNotDuplicatedEmail(email); // 이메일 중복 여부 확인
        emailVerificationService.sendVerificationCode(email);
    }

    public void verifyEmail( String email, String code) { // 인증코드 확인
        email = normalizeEmail(email);
        emailVerificationService.verifyEmail(email, code);
    }

    public void consumeVerifiedEmail(String email){ // 인증 완료된 메일인지 확인
        emailVerificationService.consumeVerifiedEmail(email);
    }

    public UserTokenDto login(String email, String password) {
        // 1. User 검증
        email = normalizeEmail(email);
        User user = userRepository.findByEmail(email).orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));
        checkUserStatus(user.getStatus());

        // 2. 비밀번호 검증
        password = password.strip();
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new CustomException(UserErrorCode.INVALID_PASSWORD);
        }

        // 3. 토큰 발행
        Date now = new Date(); // 발급 시간 고정
        String accessTokenValue = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().toString(), now);
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId(), now);

        // 4. 리프레시 토큰 저장
        saveRefreshToken(refreshTokenValue, now, user.getId());

        return new UserTokenDto(accessTokenValue, refreshTokenValue);
    }

    public UserTokenDto reissue(String savedRefreshTokenValue) {
        // 1. 가져온 refreshToken 검증
        if(savedRefreshTokenValue==null){
            throw new CustomException(TokenErrorCode.TOKEN_NOT_FOUND);
        }

        // 1-2. 서명, 알고리즘, 만료 시간 검증
        jwtTokenProvider.validateToken(savedRefreshTokenValue);

        // 2. 유저 검증
        Long userId = jwtTokenProvider.getUserIdFromToken(savedRefreshTokenValue); // userId 추출
        User user = userRepository.findById(userId).orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        checkUserStatus(user.getStatus());

        // 3. 토큰 탈취 여부 검증 및 처리 - userId로 토큰 탐색, 탈취된 토큰이라면 삭제 처리
        RefreshToken refreshToken= refreshTokenRepository.findByUserId(userId)
                .orElseThrow(()-> new CustomException(TokenErrorCode.TOKEN_NOT_FOUND));

        if (!refreshToken.getTokenValue().equals(savedRefreshTokenValue)) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException(TokenErrorCode.REUSED_TOKEN);
        }

        // 4. 토큰 발행 (RTR로 refreshToken도 발행)
        Date now = new Date(); // 발급 시간 고정
        String accessTokenValue = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().toString(), now);

        // 4-2. RTR - 리프레시 토큰 재발행 및 저장
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId(), now);
        saveRefreshToken(refreshTokenValue, now, user.getId());

        return new UserTokenDto(accessTokenValue, refreshTokenValue);

    }

    public void logout(Long userId){
        refreshTokenRepository.deleteByUserIdCustom(userId); // refreshToken 삭제
    }

    private void checkUserStatus(UserStatus userStatus){ // 계정 상태에 따른 처리
        if(userStatus.equals(UserStatus.SUSPENDED)){ // 휴면
            throw new CustomException(UserErrorCode.SUSPENDED_STATUS);
        }
        if(userStatus.equals(UserStatus.DORMANT)){ // 정지
            throw new CustomException(UserErrorCode.DORMANT_STATUS);
        }
        if(userStatus.equals(UserStatus.WITHDRAWN)){ // 탈퇴
            throw new CustomException(UserErrorCode.WITHDRAWN_STATUS);
        }
        if(userStatus.equals(UserStatus.BANNED)){ // 탈퇴
            throw new CustomException(UserErrorCode.BANNED_STATUS);
        }
    }

    private void saveRefreshToken(String refreshTokenValue, Date now, Long userId){ // refreshToken 저장
        Date expiration = new Date (now.getTime() + 14 * 24 * 60 * 60 * 1000L); //refresh 만료 시간 계산
        LocalDateTime expiredAt = LocalDateTime.ofInstant( //type 변환
                expiration.toInstant(),
                ZoneId.systemDefault()
        );
        refreshTokenRepository.findByUserId(userId).ifPresentOrElse(
                // 이전에 만료된 토큰이 존재하는 경우(찌꺼기)
                existingToken -> existingToken.update(refreshTokenValue, expiredAt),
                // 없는 경우
                ()-> {
                    RefreshToken refreshToken = new RefreshToken(userId, refreshTokenValue, expiredAt);
                    refreshTokenRepository.save(refreshToken);
                }
        );
    }

    private String normalizeEmail(String email) { // 이메일 형식 변환
        return email.strip().toLowerCase();
    }
    private void consumeNotDuplicatedEmail(String email){ // 이메일 중복 확인
        if(userRepository.existsByEmail(email)){
            throw new CustomException(UserErrorCode.DUPLICATED_USER);
        }
        // 1-2. 탈퇴한 유저 중에서도 확인
        if(withdrawnUserRepository.existsByEncodedEmail(withdrawnEncoder.encode(email))){
            throw new CustomException(UserErrorCode.UNUSABLE_EMAIL);
        }
    }
}
