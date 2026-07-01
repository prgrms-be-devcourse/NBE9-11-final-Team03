package com.back.baton.domain.credit.dto.response;

import com.back.baton.domain.credit.entity.CreditTransaction;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "크레딧 거래 내역 조회 응답 DTO")
public record CreditTransactionRes(
        Long transactionId,
        Long relatedTradeId,
        CreditTransactionType type,
        Integer amount,
        Integer balanceAfter,
        String defaultReason,
        String detailReason,
        LocalDateTime createdAt
) {
    public static CreditTransactionRes from(CreditTransaction transaction) {
        return new CreditTransactionRes(
                transaction.getId(),
                transaction.getRelatedTradeId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDefaultReason(),
                transaction.getDetailReason(),
                transaction.getCreatedAt()
        );
    }
}
