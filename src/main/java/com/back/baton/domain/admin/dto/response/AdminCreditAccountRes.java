package com.back.baton.domain.admin.dto.response;

import com.back.baton.domain.credit.entity.CreditAccount;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 유저 크레딧 잔액 응답")
public record AdminCreditAccountRes(
        @Schema(description = "크레딧 계좌 ID", example = "1")
        Long accountId,

        @Schema(description = "유저 ID", example = "2")
        Long userId,

        @Schema(description = "사용 가능 크레딧 잔액", example = "900")
        int balance,

        @Schema(description = "에스크로에 보류된 크레딧 잔액", example = "100")
        int escrowBalance
) {
    public static AdminCreditAccountRes from(CreditAccount account) {
        return new AdminCreditAccountRes(
                account.getId(),
                account.getUserId(),
                account.getBalance(),
                account.getEscrowBalance()
        );
    }
}
