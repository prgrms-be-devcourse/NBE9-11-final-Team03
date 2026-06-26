package com.back.baton.domain.credit.service;

import com.back.baton.domain.credit.dto.request.CreditTransactionSearchReq;
import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.entity.CreditTransaction;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.domain.credit.repository.CreditTransactionRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.CreditErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditService {

    private final CreditAccountRepository creditAccountRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    @Value("${credit.initial-amount}")
    private int initialCreditAmount;

    @Value("${credit.transaction-max-page-size}")
    private int maxPageSize;

    // 크레딧 잔액 조회
    public CreditBalanceRes getBalance(Long userId) {
        CreditAccount creditAccount = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        return CreditBalanceRes.from(creditAccount);
    }

    // 본인 크레딧 거래 내역 조회
    public CursorPageRes<CreditTransactionRes> getTransactionHistory(
            Long userId, CreditTransactionSearchReq req, Long cursor, int size
    ) {
        CreditTransactionSearchReq searchReq = req != null ? req : new CreditTransactionSearchReq(null, null, null); // NPE 방지를 위한 기본값 할당
        int pageSize = Math.clamp(size, 1, maxPageSize);
        List<CreditTransactionRes> rows = creditTransactionRepository.findHistory(userId, searchReq, cursor, pageSize);

        return CursorPageRes.from(rows, pageSize, CreditTransactionRes::transactionId);
    }

    // 초기 크레딧 계좌 생성 및 지급
    @Transactional
    public void initializeAccount(Long userId) {
        if (creditAccountRepository.findByUserId(userId).isPresent()) {
            throw new CustomException(CreditErrorCode.CREDIT_ACCOUNT_ALREADY_EXISTS);
        }
        creditAccountRepository.save(CreditAccount.create(userId, initialCreditAmount));
        creditTransactionRepository.save(CreditTransaction.create(
                userId,
                null,
                CreditTransactionType.WELCOME, initialCreditAmount,
                initialCreditAmount,
                null
        ));
    }

    // 크레딧 잠금 - 매칭 제안 수락 시 구매자 크레딧을 거래 완료까지 동결 (거래 취소 시 환불 가능)
    @Transactional
    public void holdForEscrow(Long userId, int amount, Long relatedTradeId) {
        validatePositiveAmount(amount);

        int updatedRows = creditAccountRepository.holdForEscrow(userId, amount);
        if (updatedRows == 0) {
            creditAccountRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
            throw new CustomException(CreditErrorCode.INSUFFICIENT_CREDIT_BALANCE);
        }

        int balanceAfter = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND))
                .getBalance();

        // 거래 내역 기록
        creditTransactionRepository.save(CreditTransaction.create(
                userId,
                relatedTradeId,
                CreditTransactionType.ESCROW_HOLD,
                -amount,
                balanceAfter,
                null
        ));
    }

    // 크레딧 환불 - 거래 취소 시 에스크로에서 구매자에게 크레딧 반환
    @Transactional
    public void refundFromEscrow(Long userId, int amount, Long relatedTradeId) {
        validatePositiveAmount(amount);

        int updatedRows = creditAccountRepository.releaseEscrow(userId, amount);
        if (updatedRows == 0) {
            creditAccountRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));
            throw new CustomException(CreditErrorCode.INSUFFICIENT_ESCROW_BALANCE);
        }

        int balanceAfter = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND))
                .getBalance();

        creditTransactionRepository.save(CreditTransaction.create(
                userId,
                relatedTradeId,
                CreditTransactionType.REFUND,
                amount,
                balanceAfter,
                null
        ));
    }

    // 크레딧 정산 - 구매 확정 시 구매자 에스크로 차감 후 판매자에게 지급
    @Transactional
    public void settleEscrow(Long buyerId, Long sellerId, int amount, int settlementAmount, Long tradeId) {

        validatePositiveAmount(amount);
        validatePositiveAmount(settlementAmount);

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
        int sellerUpdatedRows = creditAccountRepository.addBalance(sellerId, settlementAmount); // 수수료 반영된 금액 적립
        if (sellerUpdatedRows == 0) {
            throw new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND);
        }

        // 판매자 잔액 조회
        int sellerBalanceAfter = creditAccountRepository.findByUserId(sellerId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND))
                .getBalance();

        // 구매자 거래 내역 기록
        creditTransactionRepository.save(CreditTransaction.create(
                buyerId,
                tradeId,
                CreditTransactionType.ESCROW_RELEASE,
                -amount,
                buyerBalanceAfter,
                null
        ));

        // 판매자 거래 내역 기록
        creditTransactionRepository.save(CreditTransaction.create(
                sellerId,
                tradeId,
                CreditTransactionType.ESCROW_RELEASE,
                settlementAmount,
                sellerBalanceAfter,
                null
        ));
    }

    private void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new CustomException(CreditErrorCode.INVALID_CREDIT_AMOUNT);
        }
    }
}