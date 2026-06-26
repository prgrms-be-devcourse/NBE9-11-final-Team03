package com.back.baton.domain.talent.repository;

import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.TalentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TalentReportRepository extends JpaRepository<TalentReport, Long> {

    // 중복 신고 방지 (같은 reporter가 같은 talent 재신고 차단)
    boolean existsByTalentIdAndReporterId(Long talentId, Long reporterId);

    // 관리자 신고 목록 조회 필터 검색.
    @Query(value = """
            SELECT r
            FROM TalentReport r
            JOIN FETCH r.talent t
            WHERE (:status IS NULL OR r.status = :status)
              AND (:reason IS NULL OR r.reason = :reason)
            """,
            countQuery = """
            SELECT COUNT(r)
            FROM TalentReport r
            WHERE (:status IS NULL OR r.status = :status)
              AND (:reason IS NULL OR r.reason = :reason)
            """)
    Page<TalentReport> searchAdminReports(
            @Param("status") ReportStatus status,
            @Param("reason") ReportReason reason,
            Pageable pageable
    );
}
