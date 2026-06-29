package com.back.baton.domain.trade.service;

import com.back.baton.domain.trade.entity.TradeGroup;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeGroupRepository;
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
class TradeGroupServiceTest {

    @InjectMocks
    private TradeGroupService tradeGroupService;

    @Mock
    private TradeGroupRepository tradeGroupRepository;

    @Test
    @DisplayName("TradeGroup 생성 시 올바른 필드로 저장된다")
    void create_savedWithCorrectFields() {
        ArgumentCaptor<TradeGroup> captor = ArgumentCaptor.forClass(TradeGroup.class);
        given(tradeGroupRepository.saveAndFlush(any())).willAnswer(inv -> inv.getArgument(0));

        tradeGroupService.create(42L, TradeType.SWAP);

        then(tradeGroupRepository).should().saveAndFlush(captor.capture());
        assertThat(captor.getValue().getMatchProposalId()).isEqualTo(42L);
        assertThat(captor.getValue().getTradeType()).isEqualTo(TradeType.SWAP);
    }

    @Test
    @DisplayName("TradeGroup 생성 결과를 반환한다")
    void create_returnsCreatedGroup() {
        given(tradeGroupRepository.saveAndFlush(any())).willAnswer(inv -> inv.getArgument(0));

        TradeGroup result = tradeGroupService.create(42L, TradeType.PURCHASE);

        assertThat(result.getMatchProposalId()).isEqualTo(42L);
        assertThat(result.getTradeType()).isEqualTo(TradeType.PURCHASE);
    }
}