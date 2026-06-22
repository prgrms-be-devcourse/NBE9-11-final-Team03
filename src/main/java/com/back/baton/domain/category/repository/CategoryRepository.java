package com.back.baton.domain.category.repository;

import com.back.baton.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    // 활성 카테고리만 노출
    List<Category> findByActiveTrueOrderBySortOrderAsc();
}