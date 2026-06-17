package com.back.baton.domain.credit.service;

import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.CreditErrorCode;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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
                        .isEqualTo(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("초기 크레딧 계좌가 정상적으로 생성된다")
    void initializeAccount_success() {
        ReflectionTestUtils.setField(creditService, "initialCreditAmount", 10000);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.empty());

        creditService.initializeAccount(1L);

        then(creditAccountRepository).should().save(any(CreditAccount.class));
    }

    @Test
    @DisplayName("이미 크레딧 계좌가 존재하면 CREDIT_ACCOUNT_ALREADY_EXISTS 예외가 발생한다")
    void initializeAccount_alreadyExists() {
        CreditAccount existingAccount = new CreditAccount();
        ReflectionTestUtils.setField(existingAccount, "userId", 1L);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(existingAccount));

        assertThatThrownBy(() -> creditService.initializeAccount(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.CREDIT_ACCOUNT_ALREADY_EXISTS));

        then(creditAccountRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("적립 금액이 양수이고 계좌가 존재하면 잔액이 증가한다")
    void earnCredit_success() {
        given(creditAccountRepository.addBalance(1L, 5000)).willReturn(1);

        creditService.earnCredit(1L, 5000);

        then(creditAccountRepository).should().addBalance(1L, 5000);
    }

    @Test
    @DisplayName("적립 금액이 0 이하이면 INVALID_CREDIT_AMOUNT 예외가 발생한다")
    void earnCredit_invalidAmount() {
        assertThatThrownBy(() -> creditService.earnCredit(1L, 0))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INVALID_CREDIT_AMOUNT));

        then(creditAccountRepository).should(never()).addBalance(any(), anyInt());
    }

    @Test
    @DisplayName("크레딧 계좌가 없으면 CREDIT_ACCOUNT_NOT_FOUND 예외가 발생한다")
    void earnCredit_accountNotFound() {
        given(creditAccountRepository.addBalance(999L, 5000)).willReturn(0);

        assertThatThrownBy(() -> creditService.earnCredit(999L, 5000))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
    }
}