package com.back.baton.domain.category.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder; // 노출 순서

    @Column(name = "is_active", nullable = false)
    private boolean active;


    public static Category create(String name, int sortOrder) {
        Category category = new Category();
        category.name = name;
        category.sortOrder = sortOrder;
        category.active = true;   // 생성 시 노출 활성
        return category;
    }
}