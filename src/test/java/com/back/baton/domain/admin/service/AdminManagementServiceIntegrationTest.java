package com.back.baton.domain.admin.service;

import com.back.baton.domain.admin.dto.request.AdminReportResolveReq;
import com.back.baton.domain.admin.dto.request.AdminTalentStatusUpdateReq;
import com.back.baton.domain.admin.dto.request.AdminUserStatusUpdateReq;
import com.back.baton.domain.admin.dto.response.AdminTalentReportRes;
import com.back.baton.domain.admin.dto.response.AdminTalentRes;
import com.back.baton.domain.admin.dto.response.AdminUserRes;
import com.back.baton.domain.admin.entity.AdminActionTargetType;
import com.back.baton.domain.admin.entity.AdminActionType;
import com.back.baton.domain.admin.repository.AdminActionLogRepository;
import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentReport;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentReportRepository;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.exception.CustomException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "jwt.secret=admin-management-test-secret-key",
        "hash.salt=admin-management-test-salt",
        "app.mail.from=admin-management-test@baton.local"
})
@Transactional
class AdminManagementServiceIntegrationTest {

    @Autowired
    private AdminManagementService adminManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private TalentReportRepository talentReportRepository;

    @Autowired
    private AdminActionLogRepository adminActionLogRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("유저 상태를 변경하고 관리자 조치 이력을 기록한다")
    void updateUserStatus() {
        User admin = saveUser("admin-user@test.com");
        User target = saveUser("target-user@test.com");

        AdminUserRes response = adminManagementService.updateUserStatus(
                admin.getId(),
                target.getId(),
                new AdminUserStatusUpdateReq(UserStatus.SUSPENDED, "테스트 정지")
        );

        assertThat(response.status()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(adminActionLogRepository.findAll()).anySatisfy(log -> {
            assertThat(log.getTargetType()).isEqualTo(AdminActionTargetType.USER);
            assertThat(log.getActionType()).isEqualTo(AdminActionType.USER_STATUS_CHANGED);
        });
    }

    @Test
    @DisplayName("관리자가 자기 자신 상태를 변경하면 실패한다")
    void updateOwnStatus() {
        User admin = saveUser("admin-self@test.com");

        assertThatThrownBy(() -> adminManagementService.updateUserStatus(
                admin.getId(),
                admin.getId(),
                new AdminUserStatusUpdateReq(UserStatus.SUSPENDED, "자기 정지")
        )).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("재능 상태 변경과 신고 처리를 수행하고 관리자 조치 이력을 기록한다")
    void updateTalentAndResolveReport() {
        User admin = saveUser("admin-talent@test.com");
        User author = saveUser("author-talent@test.com");
        User reporter = saveUser("reporter-talent@test.com");
        Category category = categoryRepository.save(Category.create("관리자테스트", 100));
        Talent talent = talentRepository.save(Talent.create(author.getId(), category, "재능", "내용", 2, 100));
        TalentReport report = talentReportRepository.save(
                TalentReport.create(talent, reporter.getId(), ReportReason.INAPPROPRIATE_CONTENT, "부적절합니다.")
        );

        AdminTalentRes talentResponse = adminManagementService.updateTalentStatus(
                admin.getId(),
                talent.getId(),
                new AdminTalentStatusUpdateReq(TalentStatus.CLOSED, "신고 누적")
        );
        AdminTalentReportRes reportResponse = adminManagementService.resolveReport(
                admin.getId(),
                report.getId(),
                new AdminReportResolveReq("처리 완료")
        );

        assertThat(talentResponse.status()).isEqualTo(TalentStatus.CLOSED);
        assertThat(reportResponse.status()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(adminActionLogRepository.findAll()).anySatisfy(log ->
                assertThat(log.getActionType()).isEqualTo(AdminActionType.TALENT_STATUS_CHANGED)
        );
        assertThat(adminActionLogRepository.findAll()).anySatisfy(log ->
                assertThat(log.getActionType()).isEqualTo(AdminActionType.REPORT_RESOLVED)
        );
    }

    @Test
    @DisplayName("삭제된 재능 상태 변경 시 실패한다")
    void updateDeletedTalentStatus() {
        User admin = saveUser("admin-deleted-talent@test.com");
        User author = saveUser("author-deleted-talent@test.com");
        Category category = categoryRepository.save(Category.create("관리자삭제테스트", 100));
        Talent talent = talentRepository.save(Talent.create(author.getId(), category, "재능", "내용", 2, 100));
        talent.softDelete();

        assertThatThrownBy(() -> adminManagementService.updateTalentStatus(
                admin.getId(),
                talent.getId(),
                new AdminTalentStatusUpdateReq(TalentStatus.CLOSED, "삭제된 재능 상태 변경")
        )).isInstanceOf(CustomException.class);
    }

    private User saveUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(email.substring(0, email.indexOf("@")))
                .introduction("관리자 테스트 사용자입니다.")
                .trustScore(new BigDecimal("50.00"))
                .build());
    }
}
