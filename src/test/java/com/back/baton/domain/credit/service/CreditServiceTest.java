package com.back.baton.domain.credit.service;

import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @InjectMocks
    private CreditService creditService;

    @Mock
    private CreditAccountRepository creditAccountRepository;

    @Test
    @DisplayName("크레딧 계좌가 존재하면 잔액을 반환한다")
    void getBalance_success() {
        CreditAccount creditAccount = new CreditAccount();
        ReflectionTestUtils.setField(creditAccount, "userId", 1L);
        ReflectionTestUtils.setField(creditAccount, "balance", 10000);
        ReflectionTestUtils.setField(creditAccount, "escrowBalance", 0);

        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(creditAccount));

        CreditBalanceRes result = creditService.getBalance(1L);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.balance()).isEqualTo(10000);
        assertThat(result.escrowBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("크레딧 계좌가 없으면 CREDIT_ACCOUNT_NOT_FOUND 예외가 발생한다")
    void getBalance_notFound() {
        given(creditAccountRepository.findByUserId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.getBalance(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
    }
}