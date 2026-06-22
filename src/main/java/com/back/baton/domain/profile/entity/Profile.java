package com.back.baton.domain.profile.entity;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.user.entity.User;
import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Profile extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private List<String> portfolioLinkList = new ArrayList<>();

    // 1. 보유 재능 리스트
    @ManyToMany
    @JoinTable(
            name = "profile_my_talents",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> myTalentCategories = new ArrayList<>();

    // 2. 원하는 재능 리스트
    @ManyToMany
    @JoinTable(
            name = "profile_want_talents",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> wantTalentCategories = new ArrayList<>(); // 원하는 재능의 category

    private boolean visible = true;

    public Profile(User user){
        this.user = user;
    }

    public void update(List<Category> myTalentCategories, List<Category> wantTalentCategories, List<String> portfolioLinkList) {

        // null인 항목: 수정하지 않을 항목 -> null이 아닌 값에 대해서만 수정

        if (myTalentCategories != null) {
            this.myTalentCategories.clear();
            this.myTalentCategories.addAll(myTalentCategories);
        }
        if (wantTalentCategories != null) {
            this.wantTalentCategories.clear();
            this.wantTalentCategories.addAll(wantTalentCategories);
        }
        if (portfolioLinkList != null) {
            this.portfolioLinkList.clear();
            this.portfolioLinkList.addAll(portfolioLinkList);
        }
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }
}


