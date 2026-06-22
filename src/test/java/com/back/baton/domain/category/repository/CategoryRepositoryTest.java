package com.back.baton.domain.category.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class CategoryRepositoryTest {

    @Autowired
    CategoryRepository categoryRepository;

    private static final List<Category> SEEDS = List.of(
            Category.create("개발", 1),
            Category.create("디자인", 2),
            Category.create("문서정리", 3)
    );

    private void seed() {
        for (Category s : SEEDS) {
            if (!categoryRepository.existsByName(s.getName())) {
                categoryRepository.save(Category.create(s.getName(), s.getSortOrder()));
            }
        }
    }

    @Test
    @DisplayName("existsByName - 저장된 카테고리는 true, 없으면 false")
    void existsByName() {
        categoryRepository.save(Category.create("개발", 1));

        assertThat(categoryRepository.existsByName("개발")).isTrue();
        assertThat(categoryRepository.existsByName("없는카테고리")).isFalse();
    }

    @Test
    @DisplayName("시드를 두 번 실행해도 카테고리는 3개로 유지된다 (멱등)")
    void seed_idempotent() {
        seed();
        seed(); // 부팅 2회 시뮬레이션

        assertThat(categoryRepository.count()).isEqualTo(3);
        assertThat(categoryRepository.findAll())
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("개발", "디자인", "문서정리");
    }

    @Test
    @DisplayName("findByActiveTrueOrderBySortOrderAsc - 활성만 sortOrder 오름차순으로 조회")
    void findByActiveTrueOrderBySortOrderAsc() {
        categoryRepository.save(Category.create("디자인", 2));
        categoryRepository.save(Category.create("개발", 1));

        Category inactive = Category.create("폐지카테고리", 3);
        ReflectionTestUtils.setField(inactive, "active", false);
        categoryRepository.save(inactive);

        List<Category> result = categoryRepository.findByActiveTrueOrderBySortOrderAsc();

        assertThat(result).extracting(Category::getName)
                .containsExactly("개발", "디자인"); // 비활성 제외 + sortOrder 순
    }
}