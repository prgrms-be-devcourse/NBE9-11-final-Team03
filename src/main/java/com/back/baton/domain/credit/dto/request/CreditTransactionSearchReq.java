package com.back.baton.domain.credit.dto.request;

import com.back.baton.domain.credit.entity.CreditTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "크레딧 거래 내역 조회 필터 요청 DTO. 모든 필드는 선택값이며, 전달하지 않으면 해당 조건은 무시됩니다.")
public record CreditTransactionSearchReq(
        CreditTransactionType type,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime to
) {
        public CreditTransactionSearchReq {
                if (from != null && to != null && from.isAfter(to)) {
                        throw new IllegalArgumentException("조회 시작일(from)은 종료일(to)보다 이전이어야 합니다.");
                }
        }
}