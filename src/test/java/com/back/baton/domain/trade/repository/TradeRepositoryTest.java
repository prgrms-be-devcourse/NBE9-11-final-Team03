package com.back.baton.domain.trade.repository;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class TradeRepositoryTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private EscrowRepository escrowRepository;

    @Test
    @DisplayName("findExpiredUnderReviewTrades - 만료일이 지난 UNDER_REVIEW 거래만 조회된다")
    void findExpiredUnderReviewTrades_success() {
        // 만료된 UNDER_REVIEW 거래 생성 (조회 대상)
        Trade trade1 = tradeRepository.save(Trade.create(1L, null, 10L, 2L, 3L, 5000, TradeType.PURCHASE));
        trade1.submitResult(); // UNDER_REVIEW 로 상태 전이
        tradeRepository.saveAndFlush(trade1);

        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(1); // 1분 전 만료
        escrowRepository.save(Escrow.createHeld(trade1.getId(), 2L, 3L, 5000, 500, 4500, expiredTime));

        // 만료되지 않은 UNDER_REVIEW 거래 생성 (대상 아님)
        Trade trade2 = tradeRepository.save(Trade.create(2L, null, 11L, 2L, 3L, 5000, TradeType.PURCHASE));
        trade2.submitResult();
        tradeRepository.saveAndFlush(trade2);

        LocalDateTime futureTime = LocalDateTime.now().plusDays(7); // 7일 후 만료
        escrowRepository.save(Escrow.createHeld(trade2.getId(), 2L, 3L, 5000, 500, 4500, futureTime));

        // 만료되었으나 IN_PROGRESS 상태인 거래 생성 (대상 아님)
        Trade trade3 = tradeRepository.save(Trade.create(3L, null, 12L, 2L, 3L, 5000, TradeType.PURCHASE));
        LocalDateTime expiredTime2 = LocalDateTime.now().minusMinutes(5);
        escrowRepository.save(Escrow.createHeld(trade3.getId(), 2L, 3L, 5000, 500, 4500, expiredTime2));

        // 쿼리 실행
        List<Trade> result = tradeRepository.findExpiredUnderReviewTrades(LocalDateTime.now());

        // 검증
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(trade1.getId());
    }
}