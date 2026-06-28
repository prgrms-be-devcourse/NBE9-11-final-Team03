package com.back.baton.domain.trade.scheduler;

import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TradeSchedulerIntegrationTest {

    @Autowired
    private TradeScheduler tradeScheduler;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private EscrowRepository escrowRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    @Test
    @DisplayName("자동 구매 확정 및 실제 슬랙 알림 전송 통합 테스트")
    @Transactional
    void testAutoConfirmAndSlackNotification() throws InterruptedException {
        // 테스트용 구매자 및 판매자 유저 데이터 적재
        User buyer = userRepository.save(User.builder()
                .email("buyer_test@test.com")
                .password("password")
                .nickname("구매자테스트")
                .introduction("구매자 소개")
                .trustScore(BigDecimal.valueOf(50.0))
                .build());

        User seller = userRepository.save(User.builder()
                .email("seller_test@test.com")
                .password("password")
                .nickname("판매자테스트")
                .introduction("판매자 소개")
                .trustScore(BigDecimal.valueOf(50.0))
                .build());

        // 각 유저의 크레딧 계좌(CreditAccount) 생성 및 잔액 채우기
        CreditAccount buyerAccount = CreditAccount.create(buyer.getId(), 10000);
        ReflectionTestUtils.setField(buyerAccount, "escrowBalance", 1000); // 1000 크레딧
        creditAccountRepository.save(buyerAccount);

        // 판매자는 기본 잔액 0원인 계좌를 생성
        CreditAccount sellerAccount = creditAccountRepository.save(CreditAccount.create(seller.getId(), 0));

        // 테스트용 거래(Trade) 데이터 적재
        Trade trade = tradeRepository.save(Trade.create(
                9999L,          // 임의의 매칭 ID
                null,           // TradeGroupId
                10L,            // 재능 ID
                buyer.getId(),  // 구매자 ID
                seller.getId(), // 판매자 ID
                1000,           // 거래 대금 (1000 크레딧)
                TradeType.PURCHASE
        ));

        trade.submitResult();
        tradeRepository.saveAndFlush(trade);

        // 테스트용 에스크로(Escrow) 적재 (만료 시간을 10분 전으로 설정)
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10);
        Escrow escrow = escrowRepository.save(Escrow.createHeld(
                trade.getId(),
                buyer.getId(),
                seller.getId(),
                1000,
                100,
                900,
                expiredTime
        ));

        System.out.println(">>> 1. 테스트 데이터 및 잔액 세팅 완료 (거래 ID: " + trade.getId() + ")");
        System.out.println(">>> 2. 스케줄러 작동 및 실제 슬랙 발송을 시도합니다.");

        // 스케줄러 메서드 수동 호출 (즉시 정산 및 슬랙 전송 실행)
        tradeScheduler.autoConfirmExpiredTrades();

        // 비동기 슬랙 전송 대기 (3초)
        Thread.sleep(3000);

        // DB 정산 결과 검증
        Trade updatedTrade = tradeRepository.findById(trade.getId()).orElseThrow();
        Escrow updatedEscrow = escrowRepository.findById(escrow.getId()).orElseThrow();
        CreditAccount updatedBuyerAccount = creditAccountRepository.findByUserId(buyer.getId()).orElseThrow();
        CreditAccount updatedSellerAccount = creditAccountRepository.findByUserId(seller.getId()).orElseThrow();

        // 거래는 COMPLETED, 에스크로는 RELEASED 상태로 완료되었는지 검증
        assertThat(updatedTrade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(updatedEscrow.getStatus()).isEqualTo(EscrowStatus.RELEASED);

        // 구매자의 에스크로 보관 잔액이 1000 크레딧 차감되어 0원이 되었는지 검증
        assertThat(updatedBuyerAccount.getEscrowBalance()).isEqualTo(0);

        // 판매자의 계좌로 거래 대금(1000 크레딧)이 정상 정산 적립되었는지 검증
        assertThat(updatedSellerAccount.getBalance()).isEqualTo(1000);

        System.out.println(">>> 3. 데이터베이스 정산 검증 통과 및 테스트 완료!");
    }
}