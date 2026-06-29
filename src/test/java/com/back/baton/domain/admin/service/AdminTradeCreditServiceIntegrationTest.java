package com.back.baton.domain.admin.service;

import com.back.baton.domain.admin.dto.request.AdminTradeSearchReq;
import com.back.baton.domain.admin.dto.response.AdminCreditAccountRes;
import com.back.baton.domain.admin.dto.response.AdminEscrowRes;
import com.back.baton.domain.admin.dto.response.AdminPageRes;
import com.back.baton.domain.admin.dto.response.AdminTradeRes;
import com.back.baton.domain.credit.dto.response.CreditTransactionRes;
import com.back.baton.domain.credit.entity.CreditAccount;
import com.back.baton.domain.credit.entity.CreditTransaction;
import com.back.baton.domain.credit.entity.CreditTransactionType;
import com.back.baton.domain.credit.repository.CreditAccountRepository;
import com.back.baton.domain.credit.repository.CreditTransactionRepository;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=admin-trade-credit-test-secret-key",
        "hash.salt=admin-trade-credit-test-salt",
        "app.mail.from=admin-trade-credit-test@baton.local"
})
@Transactional
class AdminTradeCreditServiceIntegrationTest {

    @Autowired
    private AdminTradeCreditService adminTradeCreditService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private EscrowRepository escrowRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    @Autowired
    private CreditTransactionRepository creditTransactionRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("관리자가 거래, 에스크로, 유저 크레딧 정보를 조회한다")
    void getTradeEscrowAndCredit() {
        User buyer = saveUser("buyer-credit@test.com");
        User seller = saveUser("seller-credit@test.com");
        Long buyerId = buyer.getId();
        Long sellerId = seller.getId();
        Long talentId = 9303L;

        Trade trade = tradeRepository.save(Trade.create(
                9301L,
                null,
                talentId,
                buyerId,
                sellerId,
                100,
                TradeType.PURCHASE
        ));
        User otherBuyer = saveUser("other-buyer-credit@test.com");
        tradeRepository.save(Trade.create(
                9302L,
                null,
                9304L,
                otherBuyer.getId(),
                sellerId,
                200,
                TradeType.PURCHASE
        ));
        Escrow escrow = escrowRepository.save(
                Escrow.createHeld(trade.getId(), buyerId, sellerId, 100, 0.1, LocalDateTime.now().plusDays(7))
        );
        creditAccountRepository.save(CreditAccount.create(buyerId, 900));
        creditTransactionRepository.save(CreditTransaction.create(
                buyerId,
                trade.getId(),
                CreditTransactionType.ESCROW_HOLD,
                -100,
                900,
                "관리자 테스트 에스크로 예치"
        ));

        AdminPageRes<AdminTradeRes> tradeListResponse = adminTradeCreditService.getTrades(
                new AdminTradeSearchReq(
                        TradeStatus.IN_PROGRESS,
                        buyerId,
                        sellerId,
                        TradeType.PURCHASE
                ),
                PageRequest.of(0, 10)
        );
        TradeRes tradeResponse = adminTradeCreditService.getTrade(trade.getId());
        AdminEscrowRes escrowResponse = adminTradeCreditService.getEscrow(trade.getId());
        AdminCreditAccountRes creditResponse = adminTradeCreditService.getCreditAccount(buyerId);
        AdminPageRes<CreditTransactionRes> transactionResponse =
                adminTradeCreditService.getCreditTransactions(buyerId, PageRequest.of(0, 10));

        assertThat(tradeListResponse.content()).hasSize(1);
        assertThat(tradeListResponse.content().getFirst().tradeId()).isEqualTo(trade.getId());
        assertThat(tradeListResponse.content())
                .extracting(AdminTradeRes::buyerId)
                .containsOnly(buyerId);
        assertThat(tradeResponse.tradeId()).isEqualTo(trade.getId());
        assertThat(tradeResponse.tradeGroupId()).isNull();
        assertThat(escrowResponse.escrowId()).isEqualTo(escrow.getId());
        assertThat(creditResponse.balance()).isEqualTo(900);
        assertThat(transactionResponse.content()).hasSize(1);
        assertThat(transactionResponse.content().getFirst().relatedTradeId()).isEqualTo(trade.getId());
    }

    private User saveUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(email.substring(0, email.indexOf("@")))
                .introduction("관리자 거래 크레딧 테스트 사용자입니다.")
                .trustScore(new BigDecimal("50.00"))
                .build());
    }
}
