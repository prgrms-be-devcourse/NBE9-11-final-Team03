package com.back.baton.domain.user.service;

import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.WithdrawnUser;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.domain.user.repository.WithdrawnUserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock private WithdrawnUserRepository withdrawnUserRepository;
    @Mock private EscrowRepository escrowRepository;
    @Mock private MatchProposalRepository matchProposalRepository;
    @Mock private AuthService authService;
    @Mock private WithdrawnEncoder withdrawnEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("탈퇴 성공 - 모든 조건 만족 시")
    void withdraw_success() {
        // given
        Long userId = 1L;
        User user = spy(User.builder().email("test@test.com").build());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(escrowRepository.existsByUserIdAndStatus(any(), any(), any())).willReturn(false);

        // when
        userService.withdraw(userId);

        // then
        verify(matchProposalRepository).updateStatusWhenProviderWithdrawn(userId, MatchProposalStatus.REJECTED);
        verify(withdrawnUserRepository).save(any(WithdrawnUser.class));
        verify(user, times(1)).softDelete();
        verify(authService).logout(userId);
    }

    @Test
    @DisplayName("탈퇴 실패 - 진행중인 에스크로 존재 시 예외 발생")
    void withdraw_fail_escrow_in_progress() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@test.com").build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(escrowRepository.existsByUserIdAndStatus(any(), any(), any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.withdraw(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.ESCROW_IN_PROGRESS);
    }

}