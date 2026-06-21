package com.back.baton.domain.talent.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentAttachment;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class TalentAttachmentRepositoryTest {

    @Autowired TalentRepository talentRepository;
    @Autowired TalentAttachmentRepository talentAttachmentRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    @DisplayName("findByTalentIdOrderByIdAsc - 해당 재능의 첨부만 id 오름차순으로 조회")
    void findByTalentIdOrderByIdAsc() {
        // given: 카테고리 1개 + 재능 2개
        Category category = saveCategory("백엔드");
        Talent talent1 = talentRepository.save(Talent.create(7L, category, "재능1", "내용", 2, 100));
        Talent talent2 = talentRepository.save(Talent.create(7L, category, "재능2", "내용", 2, 100));

        // talent1에 3개(저장 순서 일부러 섞음), talent2에 1개
        talentAttachmentRepository.save(TalentAttachment.create(talent1, "url-a", "a"));
        talentAttachmentRepository.save(TalentAttachment.create(talent2, "other", "다른 재능"));
        talentAttachmentRepository.save(TalentAttachment.create(talent1, "url-b", "b"));
        talentAttachmentRepository.save(TalentAttachment.create(talent1, "url-c", "c"));

        // when
        List<TalentAttachment> result = talentAttachmentRepository.findByTalentIdOrderByIdAsc(talent1.getId());

        // then: talent1 것만 3개, id 오름차순 = 저장 순서대로
        assertThat(result).hasSize(3);
        assertThat(result).extracting(TalentAttachment::getUrl)
                .containsExactly("url-a", "url-b", "url-c");
    }

    @Test
    @DisplayName("첨부가 없는 재능은 빈 리스트")
    void findByTalentIdOrderByIdAsc_empty() {
        Category category = saveCategory("백엔드");
        Talent talent = talentRepository.save(Talent.create(7L, category, "재능", "내용", 2, 100));

        List<TalentAttachment> result = talentAttachmentRepository.findByTalentIdOrderByIdAsc(talent.getId());

        assertThat(result).isEmpty();
    }

    // 다른 레포 테스트와 동일하게 protected 생성자 reflection으로 Category 생성
    private Category saveCategory(String name) {
        try {
            Constructor<Category> ctor = Category.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            Category category = ctor.newInstance();
            ReflectionTestUtils.setField(category, "name", name);
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", true);
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
    }
}