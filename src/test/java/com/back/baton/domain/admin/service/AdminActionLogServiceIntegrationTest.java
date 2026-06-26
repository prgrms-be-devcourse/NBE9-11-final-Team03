package com.back.baton.domain.admin.service;

import com.back.baton.domain.admin.dto.request.AdminActionLogSearchReq;
import com.back.baton.domain.admin.dto.response.AdminActionLogRes;
import com.back.baton.domain.admin.dto.response.AdminPageRes;
import com.back.baton.domain.admin.entity.AdminActionTargetType;
import com.back.baton.domain.admin.entity.AdminActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=admin-action-log-test-secret-key",
        "hash.salt=admin-action-log-test-salt"
})
@Transactional
class AdminActionLogServiceIntegrationTest {

    @Autowired
    private AdminActionLogService adminActionLogService;

    @Test
    @DisplayName("관리자 조치 이력을 기록하고 조회한다")
    void recordAndGetActionLogs() {
        adminActionLogService.record(
                1L,
                AdminActionTargetType.USER,
                2L,
                AdminActionType.USER_STATUS_CHANGED,
                "테스트 상태 변경"
        );

        AdminActionLogSearchReq req = new AdminActionLogSearchReq(
                1L,
                AdminActionTargetType.USER,
                2L,
                AdminActionType.USER_STATUS_CHANGED
        );

        AdminPageRes<AdminActionLogRes> response = adminActionLogService.getActionLogs(
                req,
                PageRequest.of(0, 10)
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).reason()).isEqualTo("테스트 상태 변경");
    }

    @Test
    @DisplayName("필터 없이 관리자 조치 이력 전체를 조회한다")
    void getActionLogsWithoutFilters() {
        adminActionLogService.record(
                1L,
                AdminActionTargetType.USER,
                2L,
                AdminActionType.USER_STATUS_CHANGED,
                "유저 상태 변경"
        );
        adminActionLogService.record(
                1L,
                AdminActionTargetType.REPORT,
                3L,
                AdminActionType.REPORT_RESOLVED,
                "신고 처리"
        );

        AdminActionLogSearchReq req = new AdminActionLogSearchReq(null, null, null, null);

        AdminPageRes<AdminActionLogRes> response = adminActionLogService.getActionLogs(
                req,
                PageRequest.of(0, 10)
        );

        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(2);
    }
}
