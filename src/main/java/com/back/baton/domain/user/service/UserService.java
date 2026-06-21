package com.back.baton.domain.user.service;

import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.WithdrawnUser;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.domain.user.repository.WithdrawnUserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final WithdrawnEncoder withdrawnEncoder;
    private final WithdrawnUserRepository withdrawnUserRepository;
    private final EscrowRepository escrowRepository;
    private final MatchProposalRepository matchProposalRepository;
    private final AuthService authService;
    public void withdraw(Long userId) {
        // 1. 유저 상태 확인
        User user = userRepository.findById(userId).orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 2. 진행중인 거래가 있다면 탈퇴 거부
        if(escrowRepository.existsByUserIdAndStatus(userId, userId, EscrowStatus.RELEASED)){
            throw new CustomException(UserErrorCode.ESCROW_IN_PROGRESS);
        }

        // 3. 만일 이 사람에게 들어온 제안이 있다면 전부 거절 처리
        matchProposalRepository.updateStatusWhenProviderWithdrawn(userId, MatchProposalStatus.REJECTED); // 받은 제안 전부 거절 처리

        // 그 외 탈퇴 후 정보 파기 정책을 추가한다면 여기서 구현

        // 4. 탈퇴 회원 테이블에 추가 (이메일 암호화)
        String encodedEmail = withdrawnEncoder.encode(user.getEmail());
        WithdrawnUser withdrawnUser = new WithdrawnUser(encodedEmail, user.getStatus());
        withdrawnUserRepository.save(withdrawnUser);

        // 5. 기존 user 테이블에서는 유저 삭제
        userRepository.deleteById(userId);

        // 6. 로그아웃 처리 - refreshToken 테이블에서 삭제
        authService.logout(userId);
    }
    @Value("${user.withdrawn-retention-day:90}")
    private int retentionDay;

    // 오후 3시마다 탈퇴한 사용자 중 영구정지 아닌 사용자 정보 삭제
    @Scheduled(cron = "0 0 15 * * *")
    public void deleteExpiredWithdrawalUsers() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(retentionDay);
        withdrawnUserRepository.deleteByCreatedAtBeforeAndPermanentBanIsFalse(thresholdDate);
    }
}
