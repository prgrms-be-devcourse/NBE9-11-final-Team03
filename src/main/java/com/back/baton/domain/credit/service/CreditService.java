package com.back.baton.domain.credit.service;

import com.back.baton.domain.credit.dto.response.CreditBalanceRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.CreditErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditService {

    private final CreditAccountRepository creditAccountRepository;

    @Value("${credit.initial-amount}")
    private int initialCreditAmount;

    // 크레딧 잔액 조회
    public CreditBalanceRes getBalance(Long userId) {
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
        CreditAccount account = CreditAccount.create(userId, initialCreditAmount);
        creditAccountRepository.save(account);
    }

    // 크레딧 적립
    @Transactional
    public void earnCredit(Long userId, int amount) {
        if (amount <= 0) {
            throw new CustomException(CreditErrorCode.INVALID_CREDIT_AMOUNT);
        }

        int updatedRows = creditAccountRepository.addBalance(userId, amount);
        if (updatedRows == 0) {
            throw new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND);
        }
    }

    // 크레딧 차감
    @Transactional
    public void deductCredit(Long userId, int amount) {
        if (amount <= 0) {
            throw new CustomException(CreditErrorCode.INVALID_CREDIT_AMOUNT);
        }

        int updatedRows = creditAccountRepository.deductBalance(userId, amount);
        if (updatedRows == 0) {
            creditAccountRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND)); // 계좌가 존재하지 않는 경우
            throw new CustomException(CreditErrorCode.INSUFFICIENT_CREDIT_BALANCE); // 계좌는 존재하지만 잔액이 부족한 경우
        }
    }
}