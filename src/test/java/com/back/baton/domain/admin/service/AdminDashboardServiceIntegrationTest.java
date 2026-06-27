package com.back.baton.domain.admin.service;

import com.back.baton.domain.admin.dto.response.AdminDashboardSummaryRes;
import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentReport;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentReportRepository;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=admin-dashboard-test-secret-key",
        "hash.salt=admin-dashboard-test-salt",
        "app.mail.from=admin-dashboard-test@baton.local"
})
@Transactional
class AdminDashboardServiceIntegrationTest {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private EscrowRepository escrowRepository;

    @Autowired
    private TalentReportRepository talentReportRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("관리자 대시보드 요약을 조회한다")
    void getDashboardSummary() {
        AdminDashboardSummaryRes before = adminDashboardService.getDashboardSummary();

        User buyer = saveUser("dashboard-buyer@test.com");
        User seller = saveUser("dashboard-seller@test.com");
        User reporter = saveUser("dashboard-reporter@test.com");
        Category category = categoryRepository.save(Category.create("관리자대시보드", 100));

        Talent activeTalent = talentRepository.save(Talent.create(seller.getId(), category, "재능", "내용", 2, 100));
        Talent deletedTalent = talentRepository.save(Talent.create(seller.getId(), category, "삭제재능", "내용", 2, 100));
        deletedTalent.softDelete();

        Trade trade = tradeRepository.save(Trade.create(
                10001L,
                null,
                activeTalent.getId(),
                buyer.getId(),
                seller.getId(),
                100,
                TradeType.PURCHASE
        ));
        escrowRepository.save(Escrow.createHeld(
                trade.getId(),
                buyer.getId(),
                seller.getId(),
                100,
                0,
                100,
                LocalDateTime.now().plusDays(7)
        ));
        talentReportRepository.save(TalentReport.create(
                activeTalent,
                reporter.getId(),
                ReportReason.INAPPROPRIATE_CONTENT,
                "부적절합니다."
        ));

        AdminDashboardSummaryRes response = adminDashboardService.getDashboardSummary();

        assertThat(response.totalUsers()).isEqualTo(before.totalUsers() + 3);
        assertThat(response.usersByStatus().get(UserStatus.ACTIVE.name()))
                .isEqualTo(before.usersByStatus().get(UserStatus.ACTIVE.name()) + 3);
        assertThat(response.totalTalents()).isEqualTo(before.totalTalents() + 1);
        assertThat(response.talentsByStatus().get(TalentStatus.ACTIVE.name()))
                .isEqualTo(before.talentsByStatus().get(TalentStatus.ACTIVE.name()) + 1);
        assertThat(response.totalTrades()).isEqualTo(before.totalTrades() + 1);
        assertThat(response.tradesByStatus().get(TradeStatus.IN_PROGRESS.name()))
                .isEqualTo(before.tradesByStatus().get(TradeStatus.IN_PROGRESS.name()) + 1);
        assertThat(response.totalEscrows()).isEqualTo(before.totalEscrows() + 1);
        assertThat(response.escrowsByStatus().get(EscrowStatus.HELD.name()))
                .isEqualTo(before.escrowsByStatus().get(EscrowStatus.HELD.name()) + 1);
        assertThat(response.totalReports()).isEqualTo(before.totalReports() + 1);
        assertThat(response.reportsByStatus().get(ReportStatus.PENDING.name()))
                .isEqualTo(before.reportsByStatus().get(ReportStatus.PENDING.name()) + 1);
    }

    private User saveUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(email.substring(0, email.indexOf("@")))
                .introduction("관리자 대시보드 테스트 사용자입니다.")
                .trustScore(new BigDecimal("50.00"))
                .build());
    }
}
