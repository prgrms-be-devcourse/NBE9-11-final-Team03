package com.back.baton.domain.trade.service;

import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @InjectMocks
    private TradeService tradeService;

    @Mock
    private TradeRepository tradeRepository;

    @Test
    @DisplayName("거래 생성 시 REQUESTED 상태로 저장된다")
    void create_savedWithRequestedStatus() {
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        given(tradeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        tradeService.create(1L, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        then(tradeRepository).should().save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TradeStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("거래 생성 시 전달된 필드가 올바르게 저장된다")
    void create_savedWithCorrectFields() {
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        given(tradeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        tradeService.create(1L, 10L, 20L, 30L, 5000, TradeType.PURCHASE);

        then(tradeRepository).should().save(captor.capture());
        Trade saved = captor.getValue();
        assertThat(saved.getMatchId()).isEqualTo(1L);
        assertThat(saved.getTalentId()).isEqualTo(10L);
        assertThat(saved.getBuyerId()).isEqualTo(20L);
        assertThat(saved.getSellerId()).isEqualTo(30L);
        assertThat(saved.getCreditPrice()).isEqualTo(5000);
        assertThat(saved.getTradeType()).isEqualTo(TradeType.PURCHASE);
    }
}