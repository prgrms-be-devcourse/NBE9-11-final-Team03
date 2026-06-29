package com.back.baton.domain.admin.service;

import com.back.baton.domain.admin.dto.request.AdminReportResolveReq;
import com.back.baton.domain.admin.dto.request.AdminReportSearchReq;
import com.back.baton.domain.admin.dto.request.AdminTalentSearchReq;
import com.back.baton.domain.admin.dto.request.AdminTalentStatusUpdateReq;
import com.back.baton.domain.admin.dto.request.AdminUserSearchReq;
import com.back.baton.domain.admin.dto.request.AdminUserStatusUpdateReq;
import com.back.baton.domain.admin.dto.response.AdminPageRes;
import com.back.baton.domain.admin.dto.response.AdminTalentReportRes;
import com.back.baton.domain.admin.dto.response.AdminTalentRes;
import com.back.baton.domain.admin.dto.response.AdminUserRes;
import com.back.baton.domain.admin.entity.AdminActionTargetType;
import com.back.baton.domain.admin.entity.AdminActionType;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentReport;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.dto.response.TalentDetailRes;
import com.back.baton.domain.talent.repository.TalentReportRepository;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.AdminErrorCode;
import com.back.baton.global.response.code.TalentErrorCode;
import com.back.baton.global.response.code.UserErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminManagementService {

    private final UserRepository userRepository;
    private final TalentRepository talentRepository;
    private final TalentReportRepository talentReportRepository;
    private final AdminActionLogService adminActionLogService;

    // 유저 목록을 관리자 필터 조건으로 조회한다.
    public AdminPageRes<AdminUserRes> getUsers(AdminUserSearchReq req, Pageable pageable) {
        Page<AdminUserRes> users = userRepository.searchAdminUsers(
                        req.status(),
                        req.role(),
                        normalizeKeyword(req.keyword()),
                        pageable
                )
                .map(AdminUserRes::from);
        return AdminPageRes.from(users);
    }

    // 유저 상세 정보를 조회한다.
    public AdminUserRes getUser(Long userId) {
        return AdminUserRes.from(getUserOrThrow(userId));
    }

    // 유저 상태를 변경하고 관리자 조치 이력을 남긴다.
    @Transactional
    public AdminUserRes updateUserStatus(Long adminId, Long userId, AdminUserStatusUpdateReq req) {
        if (Objects.equals(adminId, userId)) {
            throw new CustomException(AdminErrorCode.SELF_STATUS_CHANGE_NOT_ALLOWED);
        }

        UserStatus status = req == null ? null : req.status();
        String reason = req == null ? null : req.reason();
        if (status == null || status == UserStatus.WITHDRAWN) {
            throw new CustomException(AdminErrorCode.INVALID_ADMIN_STATUS_CHANGE);
        }

        User user = getUserOrThrow(userId);
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new CustomException(AdminErrorCode.INVALID_ADMIN_STATUS_CHANGE);
        }

        user.changeStatus(status);
        // 상태 변경과 이력 기록은 같은 트랜잭션에서 처리한다.
        adminActionLogService.record(adminId, AdminActionTargetType.USER, userId, AdminActionType.USER_STATUS_CHANGED, reason);
        return AdminUserRes.from(user);
    }

    // 재능 목록을 관리자 필터 조건으로 조회한다.
    public AdminPageRes<AdminTalentRes> getTalents(AdminTalentSearchReq req, Pageable pageable) {
        Page<AdminTalentRes> talents = talentRepository.searchAdminTalents(
                        req.status(),
                        req.categoryId(),
                        normalizeKeyword(req.keyword()),
                        pageable
                )
                .map(AdminTalentRes::from);
        return AdminPageRes.from(talents);
    }

    // 재능 상세 정보를 조회한다.
    public TalentDetailRes getTalent(Long talentId) {
        var rows = talentRepository.findDetailById(talentId);
        if (rows.isEmpty()) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND);
        }

        Object[] row = rows.getFirst();
        Talent talent = (Talent) row[0];
        User author = (User) row[1];
        return TalentDetailRes.from(talent, author);
    }

    // 재능 상태를 변경하고 관리자 조치 이력을 남긴다.
    @Transactional
    public AdminTalentRes updateTalentStatus(Long adminId, Long talentId, AdminTalentStatusUpdateReq req) {
        TalentStatus status = req == null ? null : req.status();
        String reason = req == null ? null : req.reason();
        if (status == null || (status != TalentStatus.ACTIVE && status != TalentStatus.CLOSED)) {
            throw new CustomException(AdminErrorCode.INVALID_ADMIN_STATUS_CHANGE);
        }

        Talent talent = getTalentOrThrow(talentId);
        talent.changeStatus(status);
        // 상태 변경과 이력 기록은 같은 트랜잭션에서 처리한다.
        adminActionLogService.record(adminId, AdminActionTargetType.TALENT, talentId, AdminActionType.TALENT_STATUS_CHANGED, reason);
        return AdminTalentRes.from(talent);
    }

    // 신고 목록을 관리자 필터 조건으로 조회한다.
    public AdminPageRes<AdminTalentReportRes> getReports(AdminReportSearchReq req, Pageable pageable) {
        Page<AdminTalentReportRes> reports = talentReportRepository.searchAdminReports(
                        req.status(),
                        req.reason(),
                        pageable
                )
                .map(AdminTalentReportRes::from);
        return AdminPageRes.from(reports);
    }

    // 신고 상세 정보를 조회한다.
    public AdminTalentReportRes getReport(Long reportId) {
        return AdminTalentReportRes.from(getReportOrThrow(reportId));
    }

    // 접수된 신고를 처리 완료 상태로 변경하고 관리자 조치 이력을 남긴다.
    @Transactional
    public AdminTalentReportRes resolveReport(Long adminId, Long reportId, AdminReportResolveReq req) {
        TalentReport report = getReportOrThrow(reportId);
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new CustomException(AdminErrorCode.INVALID_ADMIN_STATUS_CHANGE);
        }

        report.resolve();
        String memo = req == null ? null : req.memo();
        // 신고 처리와 이력 기록은 같은 트랜잭션에서 처리한다.
        adminActionLogService.record(adminId, AdminActionTargetType.REPORT, reportId, AdminActionType.REPORT_RESOLVED, memo);
        return AdminTalentReportRes.from(report);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }

    private Talent getTalentOrThrow(Long talentId) {
        return talentRepository.findByIdAndDeletedAtIsNull(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));
    }

    private TalentReport getReportOrThrow(Long reportId) {
        return talentReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(AdminErrorCode.REPORT_NOT_FOUND));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
