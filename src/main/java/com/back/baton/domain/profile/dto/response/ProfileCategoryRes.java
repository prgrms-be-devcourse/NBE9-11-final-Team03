package com.back.baton.domain.profile.dto.response;

import com.back.baton.domain.category.entity.Category;

public record ProfileCategoryRes(
    Long id,
    String name,
    int sortOrder,
    boolean active
) {
    public ProfileCategoryRes(Category category) {
        this(
                category.getId(),
                category.getName(),
                category.getSortOrder(),
                category.isActive()
        );
    }
}
