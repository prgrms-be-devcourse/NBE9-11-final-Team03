package com.back.baton.domain.talent.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentSortType;
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
@Import({QueryDslConfig.class, JpaAuditingConfig.class}) // QueryDSL 빈 + createdAt 채울 Auditing
class TalentRepositoryTest {

    @Autowired TalentRepository talentRepository;
    @Autowired
    CategoryRepository categoryRepository; // 카테고리 저장용

    @Test
    @DisplayName("최신순(id desc)으로 size+1개를 조회하고, 삭제된 글은 제외한다")
    void findTalentList_basic() {
        // given: 카테고리 1개 + 재능 5개(id 1~5), 그 중 1개 soft delete
        Category category = saveCategory();
        Talent t1 = save(category, "재능1");
        Talent t2 = save(category, "재능2");
        Talent t3 = save(category, "재능3");
        Talent t4 = save(category, "재능4");
        Talent t5 = save(category, "재능5");
        t3.softDelete();                 // 삭제글
        talentRepository.save(t3);

        // when: 첫 페이지(cursor=null), size=2 -> 내부적으로 3개(size+1) 조회
        List<TalentListRes> result = talentRepository.findTalentList(null, 2, TalentSortType.LATEST);

        // then
        assertThat(result).hasSize(3);                          // size+1
        assertThat(result).extracting(TalentListRes::talentId)
                .containsExactly(t5.getId(), t4.getId(), t2.getId()); // 최신순, 삭제된 t3 제외
        assertThat(result).extracting(TalentListRes::categoryName)
                .containsOnly("백엔드");                          // 조인 잘 됨
    }

    @Test
    @DisplayName("커서 이후(id < cursor)만 조회한다")
    void findTalentList_cursor() {
        // given
        Category category = saveCategory();
        Talent t1 = save(category, "재능1");
        Talent t2 = save(category, "재능2");
        Talent t3 = save(category, "재능3");
        Talent t4 = save(category, "재능4");

        // when: t4 id를 커서로 -> 그보다 작은 id만 (t3, t2, t1)
        List<TalentListRes> result = talentRepository.findTalentList(t4.getId(), 2, TalentSortType.LATEST);

        // then: id < t4, 최신순, size+1=3개
        assertThat(result).extracting(TalentListRes::talentId)
                .containsExactly(t3.getId(), t2.getId(), t1.getId());
    }

    @Test
    @DisplayName("countByAuthorIdAndDeletedAtIsNull - 삭제되지 않은 본인 재능만 센다")
    void countByAuthorIdAndDeletedAtIsNull() {
        Category category = saveCategory();
        save(category, "재능1");          // author 1
        save(category, "재능2");          // author 1
        Talent deleted = save(category, "삭제됨"); // author 1, soft delete
        deleted.softDelete();
        talentRepository.save(deleted);
        talentRepository.save(Talent.create(2L, category, "남의재능", "내용", 2, 100)); // 다른 author

        assertThat(talentRepository.countByAuthorIdAndDeletedAtIsNull(1L)).isEqualTo(2);
    }

    private Category saveCategory() {
        try {
            Constructor<Category> constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);          // protected 생성자 강제 접근
            Category category = constructor.newInstance();

            ReflectionTestUtils.setField(category, "name", "백엔드");
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", true);

            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
    }

    private Talent save(Category category, String title) {
        Talent talent = Talent.create(1L, category, title, "내용", 2, 100);
        return talentRepository.save(talent);
    }
}