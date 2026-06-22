package com.back.baton.domain.category.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    @DisplayName("카테고리 생성 시 active=true이고 전달 필드가 설정된다")
    void create() {
        Category category = Category.create("개발", 1);

        assertThat(category.getName()).isEqualTo("개발");
        assertThat(category.getSortOrder()).isEqualTo(1);
        assertThat(category.isActive()).isTrue();
    }
}