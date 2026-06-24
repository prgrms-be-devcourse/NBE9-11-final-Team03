package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.talent.dto.request.TalentReportReq;
import com.back.baton.domain.talent.dto.response.TalentReportRes;
import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentReport;
import com.back.baton.domain.talent.repository.TalentReportRepository;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TalentReportServiceTest {

    @InjectMocks TalentReportService talentReportService;
    @Mock TalentRepository talentRepository;
    @Mock TalentReportRepository talentReportRepository;

    private static final Long TALENT_ID = 1L;
    private static final Long AUTHOR_ID = 2L;
    private static final Long REPORTER_ID = 99L;

    // 작성자 AUTHOR_ID 소유의 활성 재능 실객체
    private Talent activeTalent() {
        Talent talent = Talent.create(AUTHOR_ID, mock(Category.class), "제목", "내용", 2, 100);
        ReflectionTestUtils.setField(talent, "id", TALENT_ID);
        return talent;
    }

    @Test
    @DisplayName("타인의 활성 재능을 신고하면 PENDING 상태로 저장된다")
    void reportTalent_success() {
        Talent talent = activeTalent();
        given(talentRepository.findByIdAndDeletedAtIsNull(TALENT_ID)).willReturn(Optional.of(talent));
        given(talentReportRepository.existsByTalentIdAndReporterId(TALENT_ID, REPORTER_ID)).willReturn(false);
        given(talentReportRepository.save(any(TalentReport.class)))
                .willAnswer(invocation -> {
                    TalentReport saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 10L);
                    return saved;
                });

        var req = new TalentReportReq(ReportReason.INAPPROPRIATE_CONTENT, "부적절합니다");
        TalentReportRes res = talentReportService.reportTalent(TALENT_ID, REPORTER_ID, req);

        assertThat(res.reportId()).isEqualTo(10L);
        assertThat(res.talentId()).isEqualTo(TALENT_ID);
        assertThat(res.status()).isEqualTo(ReportStatus.PENDING);
        then(talentReportRepository).should().save(any(TalentReport.class));
    }

    @Test
    @DisplayName("존재하지 않는 재능이면 TALENT_NOT_FOUND, 저장하지 않는다")
    void reportTalent_talentNotFound() {
        given(talentRepository.findByIdAndDeletedAtIsNull(TALENT_ID)).willReturn(Optional.empty());

        var req = new TalentReportReq(ReportReason.ETC, null);
        assertThatThrownBy(() -> talentReportService.reportTalent(TALENT_ID, REPORTER_ID, req))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.TALENT_NOT_FOUND));
        then(talentReportRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("삭제된 재능이면 TALENT_NOT_FOUND로 존재를 숨긴다")
    void reportTalent_deleted_notFound() {
        // 삭제된 재능은 findByIdAndDeletedAtIsNull에서 조회되지 않음 -> empty
        given(talentRepository.findByIdAndDeletedAtIsNull(TALENT_ID)).willReturn(Optional.empty());

        var req = new TalentReportReq(ReportReason.ETC, null);
        assertThatThrownBy(() -> talentReportService.reportTalent(TALENT_ID, REPORTER_ID, req))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.TALENT_NOT_FOUND));
        then(talentReportRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("본인 재능을 신고하면 SELF_REPORT_NOT_ALLOWED(403), 저장하지 않는다")
    void reportTalent_self_forbidden() {
        Talent talent = activeTalent();
        given(talentRepository.findByIdAndDeletedAtIsNull(TALENT_ID)).willReturn(Optional.of(talent));

        var req = new TalentReportReq(ReportReason.ETC, null);
        // reporter = 작성자 본인(AUTHOR_ID)
        assertThatThrownBy(() -> talentReportService.reportTalent(TALENT_ID, AUTHOR_ID, req))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.SELF_REPORT_NOT_ALLOWED));
        then(talentReportRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이미 신고한 재능이면 DUPLICATE_REPORT(409), 저장하지 않는다")
    void reportTalent_duplicate_conflict() {
        Talent talent = activeTalent();
        given(talentRepository.findByIdAndDeletedAtIsNull(TALENT_ID)).willReturn(Optional.of(talent));
        given(talentReportRepository.existsByTalentIdAndReporterId(TALENT_ID, REPORTER_ID)).willReturn(true);

        var req = new TalentReportReq(ReportReason.ETC, null);
        assertThatThrownBy(() -> talentReportService.reportTalent(TALENT_ID, REPORTER_ID, req))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(TalentErrorCode.DUPLICATE_REPORT));
        then(talentReportRepository).should(never()).save(any());
    }
}