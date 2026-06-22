package com.back.baton.domain.category.service;

import com.back.baton.domain.category.dto.response.CategoryRes;
import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks CategoryService categoryService;
    @Mock CategoryRepository categoryRepository;

    @Test
    @DisplayName("활성 카테고리를 노출 순서대로 매핑해 반환한다")
    void getActiveCategories_success() {
        given(categoryRepository.findByActiveTrueOrderBySortOrderAsc())
                .willReturn(List.of(
                        Category.create("개발", 1),
                        Category.create("디자인", 2),
                        Category.create("문서정리", 3)
                ));

        List<CategoryRes> result = categoryService.getActiveCategories();

        assertThat(result).extracting(CategoryRes::name)
                .containsExactly("개발", "디자인", "문서정리");
        assertThat(result).extracting(CategoryRes::sortOrder)
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("활성 카테고리가 없으면 빈 리스트를 반환한다")
    void getActiveCategories_empty() {
        given(categoryRepository.findByActiveTrueOrderBySortOrderAsc())
                .willReturn(List.of());

        assertThat(categoryService.getActiveCategories()).isEmpty();
    }
}