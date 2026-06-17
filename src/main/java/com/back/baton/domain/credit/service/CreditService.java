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

    public CreditBalanceRes getBalance(Long userId) {
        CreditAccount creditAccount = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CreditErrorCode.CREDIT_ACCOUNT_NOT_FOUND));

        return CreditBalanceRes.from(creditAccount);
    }

    @Transactional
    public void initializeAccount(Long userId) {
        if (creditAccountRepository.findByUserId(userId).isPresent()) {
            throw new CustomException(CreditErrorCode.CREDIT_ACCOUNT_ALREADY_EXISTS);
        }
        CreditAccount account = CreditAccount.create(userId, initialCreditAmount);
        creditAccountRepository.save(account);
    }

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
}