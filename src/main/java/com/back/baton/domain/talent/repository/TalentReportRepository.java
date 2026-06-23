package com.back.baton.domain.talent.repository;

import com.back.baton.domain.talent.entity.TalentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalentReportRepository extends JpaRepository<TalentReport, Long> {

    // 중복 신고 방지 (같은 reporter가 같은 talent 재신고 차단)
    boolean existsByTalentIdAndReporterId(Long talentId, Long reporterId);
}