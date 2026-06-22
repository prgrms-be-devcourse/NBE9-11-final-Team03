package com.back.baton.domain.category.service;

import com.back.baton.domain.category.dto.response.CategoryRes;
import com.back.baton.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 활성 카테고리만 노출 순서대로 조회
    public List<CategoryRes> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(CategoryRes::from)
                .toList();
    }
}