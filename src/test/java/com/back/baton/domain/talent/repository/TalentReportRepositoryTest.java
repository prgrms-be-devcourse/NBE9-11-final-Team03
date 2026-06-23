package com.back.baton.domain.talent.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentReport;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class}) // 슬라이스에 TalentRepositoryImpl(QueryDSL) 로드 + Auditing
class TalentReportRepositoryTest {

    @Autowired TalentReportRepository talentReportRepository;
    @Autowired TalentRepository talentRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    @DisplayName("같은 reporter가 같은 talent를 신고했으면 true를 반환한다")
    void existsByTalentIdAndReporterId_true() {
        Talent talent = saveTalent();
        talentReportRepository.save(TalentReport.create(talent, 99L, ReportReason.ETC, "사유"));

        assertThat(talentReportRepository.existsByTalentIdAndReporterId(talent.getId(), 99L)).isTrue();
    }

    @Test
    @DisplayName("다른 reporter면 false를 반환한다(중복 아님)")
    void existsByTalentIdAndReporterId_false_otherReporter() {
        Talent talent = saveTalent();
        talentReportRepository.save(TalentReport.create(talent, 99L, ReportReason.ETC, "사유"));

        assertThat(talentReportRepository.existsByTalentIdAndReporterId(talent.getId(), 100L)).isFalse();
    }

    @Test
    @DisplayName("저장 시 status 기본값은 PENDING이다")
    void save_defaultStatusPending() {
        Talent talent = saveTalent();
        TalentReport saved = talentReportRepository.save(
                TalentReport.create(talent, 99L, ReportReason.INAPPROPRIATE_CONTENT, "사유"));

        TalentReport found = talentReportRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(found.getReason()).isEqualTo(ReportReason.INAPPROPRIATE_CONTENT);
    }

    private Talent saveTalent() {
        return talentRepository.save(Talent.create(1L, saveCategory(), "제목", "내용", 2, 100));
    }

    private Category saveCategory() {
        try {
            Constructor<Category> constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Category category = constructor.newInstance();
            ReflectionTestUtils.setField(category, "name", "개발");
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", true);
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
    }
}