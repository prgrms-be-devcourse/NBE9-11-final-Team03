package com.back.baton.domain.trade.scheduler;

import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.global.slack.SlackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeSchedulerTest {

    @InjectMocks
    private TradeScheduler tradeScheduler;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeService tradeService;

    @Mock
    private SlackService slackService;

    @Test
    @DisplayName("autoConfirmExpiredTrades - 만료된 거래들에 대해 자동 확정을 수행하고 성공/실패 각각 슬랙 알림을 보낸다")
    void autoConfirmExpiredTrades_successAndFailure() {
        // given
        Trade trade1 = Trade.create(1L, null, 10L, 2L, 3L, 5000, TradeType.PURCHASE);
        ReflectionTestUtils.setField(trade1, "id", 100L);

        Trade trade2 = Trade.create(2L, null, 11L, 4L, 5L, 3000, TradeType.PURCHASE);
        ReflectionTestUtils.setField(trade2, "id", 200L);

        given(tradeRepository.findExpiredUnderReviewTrades(any(LocalDateTime.class)))
                .willReturn(List.of(trade1, trade2));

        // trade1은 성공(null 반환), trade2는 실패(예외 발생)로 모킹
        given(tradeService.autoConfirm(100L)).willReturn(null);
        given(tradeService.autoConfirm(200L)).willThrow(new RuntimeException("에스크로 계좌 잔액 차감 실패"));

        // when
        tradeScheduler.autoConfirmExpiredTrades();

        // then
        verify(tradeService).autoConfirm(100L);
        verify(tradeService).autoConfirm(200L);

        // 슬랙 성공/실패 템플릿이 총 2번 발송되었는지 검증
        verify(slackService, times(2)).sendNotification(anyString());
    }
}