package com.back.baton.domain.trade.dto.request;

import com.back.baton.domain.trade.entity.TradeStatus; import jakarta.validation.constraints.Max; import jakarta.validation.constraints.Min;

/* GET 요청의 쿼리 파라미터를 DTO로 묶어 관리한다. 필터링 조건 추가 시 해당 클레스에 필드를 추가한다. */
public record TradeSearchReq(
        TradeStatus status, // 거래 상태 필터
        Long cursor,        // 페이징 기준이 되는 마지막 조회 거래 ID
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
        @Max(value = 50, message = "페이지 크기는 50 이하이어야 합니다.")
        Integer size        // 한 번에 조회할 거래 개수
) {
    public TradeSearchReq { // 클라이언트가 size를 지정하지 않는다면 20으로 고정
        if (size == null) {
            size = 20;
        }
    }
}