package com.back.baton.domain.trade.service;

import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "jwt.secret=trade-settlement-persistence-test-secret-key")
class TradeSettlementPersistenceIntegrationTest {

    @Autowired
    private TradeSubmissionService tradeSubmissionService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private EscrowRepository escrowRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("구매 확정 후 거래와 에스크로 상태가 정산 이후에도 저장된다")
    void confirmPurchase_persistsTradeAndEscrowStatusAfterSettlement() {
        // 테스트에 필요한 구매자, 판매자, 거래 금액을 준비한다.
        Long buyerId = 901L;
        Long sellerId = 902L;
        int amount = 100;

        // 구매 확정 직전 상태가 되도록 크레딧, 거래, 에스크로 데이터를 만든다.
        creditAccountRepository.saveAndFlush(CreditAccount.create(buyerId, 1000));
        creditAccountRepository.saveAndFlush(CreditAccount.create(sellerId, 1000));

        Trade trade = tradeRepository.saveAndFlush(
                Trade.create(9001L, 9002L, buyerId, sellerId, amount, TradeType.PURCHASE)
        );
        escrowRepository.saveAndFlush(
                Escrow.createHeld(trade.getId(), buyerId, sellerId, amount, LocalDateTime.now().plusDays(7))
        );
        creditService.holdForEscrow(buyerId, amount, trade.getId(), "test-hold-" + trade.getId());

        trade.submitResult();
        tradeRepository.saveAndFlush(trade);
        entityManager.clear();

        // 핵심 검증 대상인 구매 확정 메서드를 실행한다.
        TradeRes response = tradeSubmissionService.confirmPurchase(trade.getId(), buyerId);

        // 응답 DTO가 아니라 DB에 저장된 최종 상태를 재조회한다.
        entityManager.clear();
        Trade savedTrade = tradeRepository.findById(trade.getId()).orElseThrow();
        Escrow savedEscrow = escrowRepository.findByTradeId(trade.getId()).orElseThrow();
        CreditAccount buyerAccount = creditAccountRepository.findByUserId(buyerId).orElseThrow();
        CreditAccount sellerAccount = creditAccountRepository.findByUserId(sellerId).orElseThrow();

        // 응답값과 실제 저장값이 모두 구매 확정 결과와 일치하는지 검증한다.
        assertThat(response.tradeStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(response.escrowStatus()).isEqualTo(EscrowStatus.RELEASED);
        assertThat(savedTrade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(savedEscrow.getStatus()).isEqualTo(EscrowStatus.RELEASED);
        assertThat(buyerAccount.getBalance()).isEqualTo(900);
        assertThat(buyerAccount.getEscrowBalance()).isZero();
        assertThat(sellerAccount.getBalance()).isEqualTo(1100);
        assertThat(sellerAccount.getEscrowBalance()).isZero();
    }

    @Test
    @DisplayName("거래 취소 후 거래와 에스크로 상태가 환불 이후에도 저장된다")
    void cancelTrade_persistsTradeAndEscrowStatusAfterRefund() {
        // 테스트에 필요한 구매자, 판매자, 거래 금액을 준비한다.
        Long buyerId = 911L;
        Long sellerId = 912L;
        int amount = 100;

        // 취소 가능한 거래 상태가 되도록 크레딧, 거래, 에스크로 데이터를 만든다.
        creditAccountRepository.saveAndFlush(CreditAccount.create(buyerId, 1000));
        creditAccountRepository.saveAndFlush(CreditAccount.create(sellerId, 1000));

        Trade trade = tradeRepository.saveAndFlush(
                Trade.create(9011L, 9012L, buyerId, sellerId, amount, TradeType.PURCHASE)
        );
        escrowRepository.saveAndFlush(
                Escrow.createHeld(trade.getId(), buyerId, sellerId, amount, LocalDateTime.now().plusDays(7))
        );
        creditService.holdForEscrow(buyerId, amount, trade.getId(), "test-cancel-hold-" + trade.getId());
        entityManager.clear();

        // 핵심 검증 대상인 거래 취소 메서드를 실행한다.
        TradeRes response = tradeService.cancelTrade(trade.getId(), buyerId);

        // 크레딧 환불 벌크 업데이트 이후에도 Trade/Escrow 변경이 유지되는지 재조회한다.
        entityManager.clear();
        Trade savedTrade = tradeRepository.findById(trade.getId()).orElseThrow();
        Escrow savedEscrow = escrowRepository.findByTradeId(trade.getId()).orElseThrow();
        CreditAccount buyerAccount = creditAccountRepository.findByUserId(buyerId).orElseThrow();
        CreditAccount sellerAccount = creditAccountRepository.findByUserId(sellerId).orElseThrow();

        // 응답값과 실제 저장값이 모두 거래 취소 결과와 일치하는지 검증한다.
        assertThat(response.tradeStatus()).isEqualTo(TradeStatus.CANCELLED);
        assertThat(response.escrowStatus()).isEqualTo(EscrowStatus.REFUNDED);
        assertThat(savedTrade.getStatus()).isEqualTo(TradeStatus.CANCELLED);
        assertThat(savedEscrow.getStatus()).isEqualTo(EscrowStatus.REFUNDED);
        assertThat(buyerAccount.getBalance()).isEqualTo(1000);
        assertThat(buyerAccount.getEscrowBalance()).isZero();
        assertThat(sellerAccount.getBalance()).isEqualTo(1000);
        assertThat(sellerAccount.getEscrowBalance()).isZero();
    }
}
