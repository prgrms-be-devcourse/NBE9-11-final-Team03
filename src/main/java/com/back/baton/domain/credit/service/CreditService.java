package com.back.baton.domain.credit.service;

import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.entity.CreditTransaction;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.domain.credit.repository.CreditTransactionRepository;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.CreditErrorCode;
import com.back.baton.global.response.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditService {

    private final CreditAccountRepository creditAccountRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserRepository userRepository;

    @Value("${credit.initial-amount}")
    private int initialCreditAmount;

    // 크레딧 잔액 조회
    public CreditBalanceRes getBalance(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        CreditAccount creditAccount = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        return CreditBalanceRes.from(creditAccount);
    }

    // 초기 크레딧 계좌 생성 및 지급
    @Transactional
    public void initializeAccount(Long userId) {
        if (creditAccountRepository.findByUserId(userId).isPresent()) {
            throw new CustomException(CreditErrorCode.CREDIT_ACCOUNT_ALREADY_EXISTS);
        }
        creditAccountRepository.save(CreditAccount.create(userId, initialCreditAmount));
        creditTransactionRepository.save(CreditTransaction.create(
                userId, null, CreditTransactionType.WELCOME, initialCreditAmount, initialCreditAmount,
                UUID.randomUUID().toString(), null
        ));
    }

    // 크레딧 적립
    @Transactional
    public void earnCredit(Long userId, int amount, CreditTransactionType type) {
        if (amount <= 0) {
            throw new CustomException(CreditErrorCode.INVALID_CREDIT_AMOUNT);
        }

        int updatedRows = creditAccountRepository.addBalance(userId, amount);
        if (updatedRows == 0) {
            throw new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND);
        }

        int balanceAfter = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND))
                .getBalance();

        creditTransactionRepository.save(CreditTransaction.create(
                userId, null, type, amount, balanceAfter, UUID.randomUUID().toString(), null
        ));
    }

    // 크레딧 잠금 - 매칭 제안 수락 시 구매자 크레딧을 거래 완료까지 동결 (거래 취소 시 환불 가능)
    @Transactional
    public void holdForEscrow(Long userId, int amount, Long relatedTradeId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new CustomException(CreditErrorCode.INVALID_IDEMPOTENCY_KEY);
        }

        if (amount <= 0) {
            throw new CustomException(CreditErrorCode.INVALID_CREDIT_AMOUNT);
        }

        // 멱등성 체크
        if (creditTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        int updatedRows = creditAccountRepository.holdForEscrow(userId, amount);
        if (updatedRows == 0) {
            creditAccountRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
            throw new CustomException(CreditErrorCode.INSUFFICIENT_CREDIT_BALANCE);
        }

        int balanceAfter = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND))
                .getBalance();

        try {
            creditTransactionRepository.saveAndFlush(CreditTransaction.create(
                    userId, relatedTradeId, CreditTransactionType.ESCROW_HOLD, -amount, balanceAfter,
                    idempotencyKey, null
            ));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(CreditErrorCode.DUPLICATE_ESCROW_HOLD_REQUEST); // idempotencyKey unique 제약 위반 예외 처리
        }
    }

    // 크레딧 환불 - 거래 취소 시 에스크로에서 구매자에게 크레딧 반환
    @Transactional
    public void refundFromEscrow(Long userId, int amount, Long relatedTradeId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new CustomException(CreditErrorCode.INVALID_IDEMPOTENCY_KEY);
        }

        if (amount <= 0) {
            throw new CustomException(CreditErrorCode.INVALID_CREDIT_AMOUNT);
        }

        if (creditTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        int updatedRows = creditAccountRepository.releaseEscrow(userId, amount);
        if (updatedRows == 0) {
            creditAccountRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
            throw new CustomException(CreditErrorCode.INSUFFICIENT_ESCROW_BALANCE);
        }

        int balanceAfter = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND))
                .getBalance();

        try {
            creditTransactionRepository.saveAndFlush(CreditTransaction.create(
                    userId, relatedTradeId, CreditTransactionType.REFUND, amount, balanceAfter,
                    idempotencyKey, null
            ));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(CreditErrorCode.DUPLICATE_ESCROW_REFUND_REQUEST);
        }
    }

    // 크레딧 정산 - 구매 확정 시 구매자 에스크로 차감 후 판매자에게 지급
    @Transactional
    public void settleEscrow(Long buyerId, Long sellerId, int amount, int settlementAmount, Long tradeId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new CustomException(CreditErrorCode.INVALID_IDEMPOTENCY_KEY);
        }

        if (amount <= 0 || settlementAmount <= 0) {
            throw new CustomException(CreditErrorCode.INVALID_CREDIT_AMOUNT);
        }

        if (creditTransactionRepository.existsByIdempotencyKey(idempotencyKey + "-buyer")) {
            return;
        }

        // 구매자 크레딧 잔액 조회
        CreditAccount buyerAccount = creditAccountRepository.findByUserId(buyerId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
        int buyerBalanceAfter = buyerAccount.getBalance();

        // 구매자 escrowBalance 차감
        int buyerUpdatedRows = creditAccountRepository.deductEscrowBalance(buyerId, amount);
        if (buyerUpdatedRows == 0) {
            throw new CustomException(CreditErrorCode.INSUFFICIENT_ESCROW_BALANCE);
        }

        // 판매자 balance 적립
        int sellerUpdatedRows = creditAccountRepository.addBalance(sellerId, settlementAmount);
        if (sellerUpdatedRows == 0) {
            throw new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND);
        }

        // 판매자 잔액 조회
        int sellerBalanceAfter = creditAccountRepository.findByUserId(sellerId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND))
                .getBalance();

        // 거래 내역 기록
        try {
            creditTransactionRepository.saveAndFlush(CreditTransaction.create(
                    buyerId, tradeId, CreditTransactionType.ESCROW_RELEASE, -amount, buyerBalanceAfter,
                    idempotencyKey + "-buyer", null
            ));
            creditTransactionRepository.saveAndFlush(CreditTransaction.create(
                    sellerId, tradeId, CreditTransactionType.ESCROW_RELEASE, settlementAmount, sellerBalanceAfter,
                    idempotencyKey + "-seller", null
            ));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(CreditErrorCode.DUPLICATE_ESCROW_SETTLE_REQUEST);
        }
    }

    // 크레딧 차감 - 수수료 등 즉시 소멸되는 크레딧에 사용 (환불 불가)
    @Transactional
    public void deductCredit(Long userId, int amount) {
        if (amount <= 0) {
            throw new CustomException(CreditErrorCode.INVALID_CREDIT_AMOUNT);
        }

        int updatedRows = creditAccountRepository.deductBalance(userId, amount);
        if (updatedRows == 0) {
            creditAccountRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
            throw new CustomException(CreditErrorCode.INSUFFICIENT_CREDIT_BALANCE);
        }

        int balanceAfter = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND))
                .getBalance();

        creditTransactionRepository.save(CreditTransaction.create(
                userId, null, CreditTransactionType.PURCHASE_DEBIT, -amount, balanceAfter,
                UUID.randomUUID().toString(), null
        ));
    }
}