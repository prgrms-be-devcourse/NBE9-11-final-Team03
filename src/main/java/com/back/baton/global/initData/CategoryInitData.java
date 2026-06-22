package com.back.baton.global.initData;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
public class CategoryInitData {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    @Lazy
    private CategoryInitData self;

    // name, sortOrder — 추후 카테고리 추가는 여기에
    private static final List<Category> SEEDS = List.of(
            Category.create("개발", 1),
            Category.create("디자인", 2),
            Category.create("문서정리", 3)
    );

    @Bean
    public ApplicationRunner initCategory() {
        return args -> self.seed();
    }

    @Transactional
    public void seed() {
        for (Category seed : SEEDS) {
            // 이름 기준 멱등
            if (!categoryRepository.existsByName(seed.getName())) {
                categoryRepository.save(seed);
            }
        }
    }
}