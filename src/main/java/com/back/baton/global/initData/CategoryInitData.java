package com.back.baton.global.initData;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryInitData implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    // 순수 데이터만 static 보관
    private static final List<SeedInfo> SEEDS = List.of(
            new SeedInfo("개발", 1),
            new SeedInfo("디자인", 2),
            new SeedInfo("문서정리", 3)
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (SeedInfo seed : SEEDS) {
            if (!categoryRepository.existsByName(seed.name())) {
                categoryRepository.save(Category.create(seed.name(), seed.sortOrder()));
            }
        }
    }

    private record SeedInfo(String name, int sortOrder) {}
}