package com.back.baton.domain.credit.service;

import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.entity.CreditTransaction;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.domain.credit.repository.CreditTransactionRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.CreditErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

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

    @Mock
    private CreditTransactionRepository creditTransactionRepository;

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
    @DisplayName("초기 크레딧 계좌가 정상적으로 생성되고 WELCOME 원장이 기록된다")
    void initializeAccount_success() {
        ReflectionTestUtils.setField(creditService, "initialCreditAmount", 10000);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.empty());

        creditService.initializeAccount(1L);

        then(creditAccountRepository).should().save(any(CreditAccount.class));
        then(creditTransactionRepository).should().save(any(CreditTransaction.class));
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
        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("적립 금액이 양수이고 계좌가 존재하면 잔액이 증가하고 원장이 기록된다")
    void earnCredit_success() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 15000);
        given(creditAccountRepository.addBalance(1L, 5000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        creditService.earnCredit(1L, 5000, CreditTransactionType.CHARGE);

        then(creditAccountRepository).should().addBalance(1L, 5000);
        then(creditTransactionRepository).should().save(any(CreditTransaction.class));
    }

    @Test
    @DisplayName("원장에 저장되는 사유와 금액이 올바르다")
    void earnCredit_savedTransactionFields() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 15000);
        given(creditAccountRepository.addBalance(1L, 5000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        ArgumentCaptor<CreditTransaction> captor = ArgumentCaptor.forClass(CreditTransaction.class);

        creditService.earnCredit(1L, 5000, CreditTransactionType.CHARGE);

        then(creditTransactionRepository).should().save(captor.capture());
        CreditTransaction saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualTo(5000);
        assertThat(saved.getReason()).isEqualTo(CreditTransactionType.CHARGE.getDefaultReason());
        assertThat(saved.getBalanceAfter()).isEqualTo(15000);
    }

    @Test
    @DisplayName("적립 금액이 0 이하이면 INVALID_CREDIT_AMOUNT 예외가 발생한다")
    void earnCredit_invalidAmount() {
        assertThatThrownBy(() -> creditService.earnCredit(1L, 0, CreditTransactionType.CHARGE))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INVALID_CREDIT_AMOUNT));

        then(creditAccountRepository).should(never()).addBalance(any(), anyInt());
        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("크레딧 계좌가 없으면 CREDIT_ACCOUNT_NOT_FOUND 예외가 발생한다")
    void earnCredit_accountNotFound() {
        given(creditAccountRepository.addBalance(999L, 5000)).willReturn(0);

        assertThatThrownBy(() -> creditService.earnCredit(999L, 5000, CreditTransactionType.CHARGE))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("차감 금액이 양수이고 잔액이 충분하면 잔액이 감소하고 원장이 기록된다")
    void deductCredit_success() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 7000);
        given(creditAccountRepository.deductBalance(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        creditService.deductCredit(1L, 3000);

        then(creditAccountRepository).should().deductBalance(1L, 3000);
        then(creditTransactionRepository).should().save(any(CreditTransaction.class));
    }

    @Test
    @DisplayName("원장에 저장되는 사유와 금액 부호가 올바르다")
    void deductCredit_savedTransactionFields() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 7000);
        given(creditAccountRepository.deductBalance(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        ArgumentCaptor<CreditTransaction> captor = ArgumentCaptor.forClass(CreditTransaction.class);

        creditService.deductCredit(1L, 3000);

        then(creditTransactionRepository).should().save(captor.capture());
        CreditTransaction saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualTo(-3000);
        assertThat(saved.getReason()).isEqualTo(CreditTransactionType.PURCHASE_DEBIT.getDefaultReason());
        assertThat(saved.getBalanceAfter()).isEqualTo(7000);
    }

    @Test
    @DisplayName("차감 금액이 0 이하이면 INVALID_CREDIT_AMOUNT 예외가 발생한다")
    void deductCredit_invalidAmount() {
        assertThatThrownBy(() -> creditService.deductCredit(1L, 0))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INVALID_CREDIT_AMOUNT));

        then(creditAccountRepository).should(never()).deductBalance(any(), anyInt());
        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("크레딧 계좌가 없으면 CREDIT_ACCOUNT_NOT_FOUND 예외가 발생한다")
    void deductCredit_accountNotFound() {
        given(creditAccountRepository.deductBalance(999L, 3000)).willReturn(0);
        given(creditAccountRepository.findByUserId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.deductCredit(999L, 3000))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("잔액이 부족하면 INSUFFICIENT_CREDIT_BALANCE 예외가 발생한다")
    void deductCredit_insufficientBalance() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "userId", 1L);
        ReflectionTestUtils.setField(account, "balance", 1000);

        given(creditAccountRepository.deductBalance(1L, 5000)).willReturn(0);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        assertThatThrownBy(() -> creditService.deductCredit(1L, 5000))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INSUFFICIENT_CREDIT_BALANCE));

        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("잔액이 충분하면 에스크로 예치가 성공하고 원장이 기록된다")
    void holdForEscrow_success() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 7000);
        given(creditTransactionRepository.existsByIdempotencyKey("key-001")).willReturn(false);
        given(creditAccountRepository.holdForEscrow(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        creditService.holdForEscrow(1L, 3000, 10L, "key-001");

        then(creditAccountRepository).should().holdForEscrow(1L, 3000);
        then(creditTransactionRepository).should().save(any(CreditTransaction.class));
    }

    @Test
    @DisplayName("원장에 저장되는 사유, 금액 부호, 거래 ID가 올바르다")
    void holdForEscrow_savedTransactionFields() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 7000);
        given(creditTransactionRepository.existsByIdempotencyKey("key-001")).willReturn(false);
        given(creditAccountRepository.holdForEscrow(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        ArgumentCaptor<CreditTransaction> captor = ArgumentCaptor.forClass(CreditTransaction.class);

        creditService.holdForEscrow(1L, 3000, 10L, "key-001");

        then(creditTransactionRepository).should().save(captor.capture());
        CreditTransaction saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualTo(-3000);
        assertThat(saved.getRelatedTradeId()).isEqualTo(10L);
        assertThat(saved.getReason()).isEqualTo(CreditTransactionType.ESCROW_HOLD.getDefaultReason());
        assertThat(saved.getBalanceAfter()).isEqualTo(7000);
    }

    @Test
    @DisplayName("이미 처리된 멱등성 키이면 중복 예치 없이 바로 반환된다")
    void holdForEscrow_duplicateIdempotencyKey() {
        given(creditTransactionRepository.existsByIdempotencyKey("key-001")).willReturn(true);

        creditService.holdForEscrow(1L, 3000, 10L, "key-001");

        then(creditAccountRepository).should(never()).holdForEscrow(any(), anyInt());
        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("동시 요청으로 idempotencyKey unique 제약 위반 시 DUPLICATE_ESCROW_HOLD_REQUEST 예외가 발생한다")
    void holdForEscrow_duplicateKeyOnSave() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 7000);
        given(creditTransactionRepository.existsByIdempotencyKey("key-001")).willReturn(false);
        given(creditAccountRepository.holdForEscrow(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        given(creditTransactionRepository.save(any())).willThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> creditService.holdForEscrow(1L, 3000, 10L, "key-001"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.DUPLICATE_ESCROW_HOLD_REQUEST));
    }

    @Test
    @DisplayName("예치 금액이 0 이하이면 INVALID_CREDIT_AMOUNT 예외가 발생한다")
    void holdForEscrow_invalidAmount() {
        assertThatThrownBy(() -> creditService.holdForEscrow(1L, 0, 10L, "key-001"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INVALID_CREDIT_AMOUNT));

        then(creditAccountRepository).should(never()).holdForEscrow(any(), anyInt());
        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("크레딧 계좌가 없으면 CREDIT_ACCOUNT_NOT_FOUND 예외가 발생한다")
    void holdForEscrow_accountNotFound() {
        given(creditTransactionRepository.existsByIdempotencyKey("key-001")).willReturn(false);
        given(creditAccountRepository.holdForEscrow(999L, 3000)).willReturn(0);
        given(creditAccountRepository.findByUserId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.holdForEscrow(999L, 3000, 10L, "key-001"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("잔액이 부족하면 INSUFFICIENT_CREDIT_BALANCE 예외가 발생한다")
    void holdForEscrow_insufficientBalance() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "userId", 1L);
        ReflectionTestUtils.setField(account, "balance", 1000);

        given(creditTransactionRepository.existsByIdempotencyKey("key-001")).willReturn(false);
        given(creditAccountRepository.holdForEscrow(1L, 5000)).willReturn(0);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        assertThatThrownBy(() -> creditService.holdForEscrow(1L, 5000, 10L, "key-001"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INSUFFICIENT_CREDIT_BALANCE));

        then(creditTransactionRepository).should(never()).save(any());
    }
}