package com.back.baton.domain.credit.repository;

import com.back.baton.domain.credit.dto.request.CreditTransactionSearchReq;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.domain.credit.entity.CreditTransaction;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class CreditTransactionHistoryRepositoryTest {

    @Autowired
    private CreditTransactionRepository creditTransactionRepository;

    private static final AtomicInteger SEQ = new AtomicInteger();

    @Test
    @DisplayName("본인(userId) 내역만 최신순(id 내림차순)으로 조회한다")
    void findHistory_onlyOwnerOrderedByIdDesc() {
        CreditTransaction first = save(1L, CreditTransactionType.WELCOME, 10000, 10000);
        CreditTransaction second = save(1L, CreditTransactionType.CHARGE, 5000, 15000);
        save(2L, CreditTransactionType.WELCOME, 10000, 10000); // 다른 유저

        List<CreditTransactionRes> result =
                creditTransactionRepository.findHistory(1L, emptyFilter(), null, 20);

        assertThat(result)
                .extracting(CreditTransactionRes::transactionId)
                .containsExactly(second.getId(), first.getId()); // 최신순, 본인만
    }

    @Test
    @DisplayName("거래 타입으로 필터링한다")
    void findHistory_filterByType() {
        save(1L, CreditTransactionType.WELCOME, 10000, 10000);
        save(1L, CreditTransactionType.CHARGE, 5000, 15000);
        CreditTransaction refund = save(1L, CreditTransactionType.REFUND, 3000, 18000);

        var req = new CreditTransactionSearchReq(CreditTransactionType.REFUND, null, null);
        List<CreditTransactionRes> result =
                creditTransactionRepository.findHistory(1L, req, null, 20);

        assertThat(result)
                .extracting(CreditTransactionRes::transactionId)
                .containsExactly(refund.getId());
        assertThat(result.get(0).type()).isEqualTo(CreditTransactionType.REFUND);
    }

    @Test
    @DisplayName("from 이 미래 시점이면 createdAt 조건에 걸려 아무것도 조회되지 않는다")
    void findHistory_filterByFromExcludesAll() {
        save(1L, CreditTransactionType.WELCOME, 10000, 10000);

        var req = new CreditTransactionSearchReq(null, LocalDateTime.now().plusDays(1), null);
        List<CreditTransactionRes> result =
                creditTransactionRepository.findHistory(1L, req, null, 20);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("from~to 범위가 현재를 포함하면 기간 내 내역이 조회된다")
    void findHistory_filterByPeriodIncludesNow() {
        save(1L, CreditTransactionType.WELCOME, 10000, 10000);
        save(1L, CreditTransactionType.CHARGE, 5000, 15000);

        var req = new CreditTransactionSearchReq(
                null, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        List<CreditTransactionRes> result =
                creditTransactionRepository.findHistory(1L, req, null, 20);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("cursor 보다 작은 id 의 내역만 조회한다")
    void findHistory_filterByCursor() {
        CreditTransaction first = save(1L, CreditTransactionType.WELCOME, 10000, 10000);
        CreditTransaction second = save(1L, CreditTransactionType.CHARGE, 5000, 15000);
        save(1L, CreditTransactionType.REFUND, 3000, 18000); // 가장 최신(가장 큰 id)

        // second 를 마지막으로 본 커서로 전달 → second 보다 작은 id 만
        List<CreditTransactionRes> result =
                creditTransactionRepository.findHistory(1L, emptyFilter(), second.getId(), 20);

        assertThat(result)
                .extracting(CreditTransactionRes::transactionId)
                .containsExactly(first.getId());
    }

    @Test
    @DisplayName("size + 1 개까지 조회해 다음 페이지 존재 여부를 판별할 수 있게 한다")
    void findHistory_limitsToSizePlusOne() {
        for (int i = 0; i < 5; i++) {
            save(1L, CreditTransactionType.CHARGE, 1000, 1000);
        }

        List<CreditTransactionRes> result =
                creditTransactionRepository.findHistory(1L, emptyFilter(), null, 2);

        assertThat(result).hasSize(3); // size(2) + 1
    }

    private CreditTransactionSearchReq emptyFilter() {
        return new CreditTransactionSearchReq(null, null, null);
    }

    private CreditTransaction save(Long userId, CreditTransactionType type, int amount, int balanceAfter) {
        CreditTransaction tx = CreditTransaction.create(
                userId, null, type, amount, balanceAfter, null
        );
        return creditTransactionRepository.saveAndFlush(tx);
    }
}