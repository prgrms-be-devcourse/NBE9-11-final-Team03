package com.back.baton.domain.credit.dto;

import com.back.baton.domain.credit.entity.CreditAccount;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "크레딧 잔액 조회 응답 DTO")
public record CreditBalanceRes(
        Long userId,
        int balance,
        int escrowBalance
) {
    public static CreditBalanceRes from(CreditAccount creditAccount) {
        return new CreditBalanceRes(
                creditAccount.getUserId(),
                creditAccount.getBalance(),
                creditAccount.getEscrowBalance()
        );
    }
}