package com.back.baton.domain.category.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.global.config.CacheConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig({CacheConfig.class, CategoryServiceCacheTest.TestConfig.class})
class CategoryServiceCacheTest {

    @Configuration
    static class TestConfig {
        @Bean
        CategoryRepository categoryRepository() {
            return mock(CategoryRepository.class);
        }
        @Bean
        CategoryService categoryService(CategoryRepository categoryRepository) {
            return new CategoryService(categoryRepository);
        }
    }

    @Autowired CategoryService categoryService;
    @Autowired CategoryRepository categoryRepository;
    @Autowired CacheManager cacheManager;

    // 컨텍스트가 캐시되어 mock·캐시가 메서드 간 공유되므로 매 테스트 후 초기화
    @AfterEach
    void tearDown() {
        cacheManager.getCache(CacheConfig.CATEGORIES).clear();
        reset(categoryRepository);
    }

    @Test
    @DisplayName("두 번째 조회는 캐시에서 반환되어 레포지토리를 다시 조회하지 않는다")
    void getActiveCategories_isCached() {
        given(categoryRepository.findByActiveTrueOrderBySortOrderAsc())
                .willReturn(List.of(Category.create("개발", 1)));

        categoryService.getActiveCategories(); // cache miss -> DB 조회
        categoryService.getActiveCategories(); // cache hit  -> DB 미조회

        verify(categoryRepository, times(1)).findByActiveTrueOrderBySortOrderAsc();
    }

    @Test
    @DisplayName("캐시를 비우면 다시 레포지토리를 조회한다")
    void getActiveCategories_afterEvict() {
        given(categoryRepository.findByActiveTrueOrderBySortOrderAsc())
                .willReturn(List.of(Category.create("개발", 1)));

        categoryService.getActiveCategories();                  // miss → 1회
        cacheManager.getCache(CacheConfig.CATEGORIES).clear();  // 캐시 비움
        categoryService.getActiveCategories();                  // miss → 2회

        verify(categoryRepository, times(2)).findByActiveTrueOrderBySortOrderAsc();
    }
}