package com.back.baton.domain.credit.service;

import com.back.baton.domain.credit.dto.request.CreditTransactionSearchReq;
import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.entity.CreditTransaction;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.domain.credit.repository.CreditTransactionRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.global.response.code.CreditErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(creditService, "maxPageSize", 50);
    }

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
    void getBalance_creditAccountNotFound() {
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.getBalance(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("거래 내역 조회 성공 시 조회 결과를 커서 페이지로 반환한다")
    void getTransactionHistory_success() {
        CreditTransactionSearchReq req = new CreditTransactionSearchReq(null, null, null);
        List<CreditTransactionRes> rows = List.of(
                historyRow(1002L, CreditTransactionType.CHARGE, 5000, 15000),
                historyRow(1001L, CreditTransactionType.WELCOME, 10000, 10000)
        );
        given(creditTransactionRepository.findHistory(1L, req, null, 20)).willReturn(rows);

        CursorPageRes<CreditTransactionRes> result =
                creditService.getTransactionHistory(1L, req, null, 20);

        assertThat(result.content()).extracting(CreditTransactionRes::transactionId)
                .containsExactly(1002L, 1001L);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEqualTo(1001L); // 마지막 행 id
    }

    @Test
    @DisplayName("size+1 개가 조회되면 hasNext=true 이고 마지막 한 건은 잘려나간다")
    void getTransactionHistory_hasNext() {
        CreditTransactionSearchReq req = new CreditTransactionSearchReq(null, null, null);
        List<CreditTransactionRes> rows = List.of(
                historyRow(3L, CreditTransactionType.CHARGE, 1000, 3000),
                historyRow(2L, CreditTransactionType.CHARGE, 1000, 2000),
                historyRow(1L, CreditTransactionType.CHARGE, 1000, 1000)
        );
        given(creditTransactionRepository.findHistory(1L, req, null, 2)).willReturn(rows);

        CursorPageRes<CreditTransactionRes> result =
                creditService.getTransactionHistory(1L, req, null, 2);

        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(2L);
    }

    @Test
    @DisplayName("size 가 최대치(50)를 넘으면 50으로 제한해 조회한다")
    void getTransactionHistory_clampsSizeUpperBound() {
        CreditTransactionSearchReq req = new CreditTransactionSearchReq(null, null, null);
        given(creditTransactionRepository.findHistory(eq(1L), eq(req), isNull(), anyInt()))
                .willReturn(List.of());

        creditService.getTransactionHistory(1L, req, null, 100);

        then(creditTransactionRepository).should().findHistory(1L, req, null, 50);
    }

    @Test
    @DisplayName("size 가 1 미만이면 1로 제한해 조회한다")
    void getTransactionHistory_clampsSizeLowerBound() {
        CreditTransactionSearchReq req = new CreditTransactionSearchReq(null, null, null);
        given(creditTransactionRepository.findHistory(eq(1L), eq(req), isNull(), anyInt()))
                .willReturn(List.of());

        creditService.getTransactionHistory(1L, req, null, 0);

        then(creditTransactionRepository).should().findHistory(1L, req, null, 1);
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
    @DisplayName("잔액이 충분하면 에스크로 예치가 성공하고 원장이 기록된다")
    void holdForEscrow_success() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 7000);
        given(creditAccountRepository.holdForEscrow(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        creditService.holdForEscrow(1L, 3000, 10L);

        then(creditAccountRepository).should().holdForEscrow(1L, 3000);
        then(creditTransactionRepository).should().save(any(CreditTransaction.class));
    }

    @Test
    @DisplayName("원장에 저장되는 사유, 금액 부호, 거래 ID가 올바르다")
    void holdForEscrow_savedTransactionFields() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 7000);
        given(creditAccountRepository.holdForEscrow(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        ArgumentCaptor<CreditTransaction> captor = ArgumentCaptor.forClass(CreditTransaction.class);

        creditService.holdForEscrow(1L, 3000, 10L);

        then(creditTransactionRepository).should().save(captor.capture());
        CreditTransaction saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualTo(-3000);
        assertThat(saved.getRelatedTradeId()).isEqualTo(10L);
        assertThat(saved.getDefaultReason()).isEqualTo(CreditTransactionType.ESCROW_HOLD.getDefaultReason());
        assertThat(saved.getDetailReason()).isNull();
        assertThat(saved.getBalanceAfter()).isEqualTo(7000);
    }

    @Test
    @DisplayName("예치 금액이 음수이면 INVALID_CREDIT_AMOUNT 예외가 발생한다")
    void holdForEscrow_invalidAmount() {
        assertThatThrownBy(() -> creditService.holdForEscrow(1L, -1, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INVALID_CREDIT_AMOUNT));

        then(creditAccountRepository).should(never()).holdForEscrow(any(), anyInt());
        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("크레딧 계좌가 없으면 CREDIT_ACCOUNT_NOT_FOUND 예외가 발생한다")
    void holdForEscrow_accountNotFound() {
        given(creditAccountRepository.holdForEscrow(999L, 3000)).willReturn(0);
        given(creditAccountRepository.findByUserId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.holdForEscrow(999L, 3000, 10L))
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

        given(creditAccountRepository.holdForEscrow(1L, 5000)).willReturn(0);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        assertThatThrownBy(() -> creditService.holdForEscrow(1L, 5000, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INSUFFICIENT_CREDIT_BALANCE));

        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("에스크로 잔액이 충분하면 환불이 성공하고 원장이 기록된다")
    void refundFromEscrow_success() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 8000);
        given(creditAccountRepository.releaseEscrow(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        creditService.refundFromEscrow(1L, 3000, 10L);

        then(creditAccountRepository).should().releaseEscrow(1L, 3000);
        then(creditTransactionRepository).should().save(any(CreditTransaction.class));
    }

    @Test
    @DisplayName("원장에 저장되는 사유, 금액, 거래 ID가 올바르다")
    void refundFromEscrow_savedTransactionFields() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "balance", 8000);
        given(creditAccountRepository.releaseEscrow(1L, 3000)).willReturn(1);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        ArgumentCaptor<CreditTransaction> captor = ArgumentCaptor.forClass(CreditTransaction.class);

        creditService.refundFromEscrow(1L, 3000, 10L);

        then(creditTransactionRepository).should().save(captor.capture());
        CreditTransaction saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualTo(3000);
        assertThat(saved.getRelatedTradeId()).isEqualTo(10L);
        assertThat(saved.getDefaultReason()).isEqualTo(CreditTransactionType.REFUND.getDefaultReason());
        assertThat(saved.getDetailReason()).isNull();
        assertThat(saved.getBalanceAfter()).isEqualTo(8000);
    }

    @Test
    @DisplayName("환불 금액이 음수이면 INVALID_CREDIT_AMOUNT 예외가 발생한다")
    void refundFromEscrow_invalidAmount() {
        assertThatThrownBy(() -> creditService.refundFromEscrow(1L, -1, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INVALID_CREDIT_AMOUNT));

        then(creditAccountRepository).should(never()).releaseEscrow(any(), anyInt());
        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("크레딧 계좌가 없으면 CREDIT_ACCOUNT_NOT_FOUND 예외가 발생한다")
    void refundFromEscrow_accountNotFound() {
        given(creditAccountRepository.releaseEscrow(999L, 3000)).willReturn(0);
        given(creditAccountRepository.findByUserId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.refundFromEscrow(999L, 3000, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        then(creditTransactionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("에스크로 잔액이 부족하면 INSUFFICIENT_ESCROW_BALANCE 예외가 발생한다")
    void refundFromEscrow_insufficientEscrowBalance() {
        CreditAccount account = new CreditAccount();
        ReflectionTestUtils.setField(account, "userId", 1L);
        ReflectionTestUtils.setField(account, "escrowBalance", 1000);

        given(creditAccountRepository.releaseEscrow(1L, 5000)).willReturn(0);
        given(creditAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        assertThatThrownBy(() -> creditService.refundFromEscrow(1L, 5000, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(CreditErrorCode.INSUFFICIENT_ESCROW_BALANCE));

        then(creditTransactionRepository).should(never()).save(any());
    }

    private CreditTransactionRes historyRow(Long id, CreditTransactionType type, int amount, int balanceAfter) {
        return new CreditTransactionRes(
                id, null, type, amount, balanceAfter,
                type.getDefaultReason(), null, LocalDateTime.now()
        );
    }
}