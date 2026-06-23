package com.back.baton.domain.talent.service;

import com.back.baton.domain.talent.dto.request.TalentReportReq;
import com.back.baton.domain.talent.dto.response.TalentReportRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentReport;
import com.back.baton.domain.talent.repository.TalentReportRepository;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentReportService {

    private final TalentRepository talentRepository;
    private final TalentReportRepository talentReportRepository;

    // 재능 신고 (존재 -> 삭제 -> 자기신고 -> 중복)
    @Transactional
    public TalentReportRes reportTalent(Long talentId, Long reporterId, TalentReportReq req) {
        Talent talent = getActiveTalent(talentId);

        // 자기 재능 신고 차단 (소유권 위반 → 403)
        if (Objects.equals(talent.getAuthorId(), reporterId)) {
            throw new CustomException(TalentErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        // 중복 신고 차단 (409) - 규모상(동접 150) 애플리케이션 레벨 체크로 충분
        if (talentReportRepository.existsByTalentIdAndReporterId(talentId, reporterId)) {
            throw new CustomException(TalentErrorCode.DUPLICATE_REPORT);
        }

        TalentReport report = TalentReport.create(talent, reporterId, req.reason(), req.description());
        return TalentReportRes.from(talentReportRepository.save(report));
    }

    // 존재 -> 삭제 검증 (TalentAttachmentService와 동일 패턴)
    private Talent getActiveTalent(Long talentId) {
        Talent talent = talentRepository.findById(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));
        if (talent.isDeleted()) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND);
        }
        return talent;
    }
}