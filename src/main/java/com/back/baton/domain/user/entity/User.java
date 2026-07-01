package com.back.baton.domain.user.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users",
        uniqueConstraints = {
            @UniqueConstraint(name = "email 중복 제약", columnNames = {"email", "deletedAt"}),
            @UniqueConstraint(name = "nickname 중복 제약", columnNames = {"nickname", "deletedAt"})
})

public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String introduction;

    @Column(precision = 5, scale = 2)
    private BigDecimal trustScore;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String password, String nickname, String profileImageUrl, String introduction, BigDecimal trustScore){

        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;

        this.trustScore = trustScore;
        this.status = UserStatus.ACTIVE;
        this.role = UserRole.USER;
        this.deletedAt = LocalDateTime.of(1880, 6, 16,0,0,0); // 과거의 시점으로 고정
    }

    public void upgradeToAdmin() {
        this.role = UserRole.ADMIN;
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    public void softDelete(){
        this.email = "";
        this.password = "";
        this.nickname = "";
        this.profileImageUrl = null;
        this.introduction = "";

        this.trustScore = new BigDecimal("50.00");
        this.status = UserStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();

    }

    public void updateProfile(String profileImageUrl, String introduction){
        if(profileImageUrl!=null){
            this.profileImageUrl = profileImageUrl;
        }
        if(introduction!=null){
            this.introduction = introduction;
        }
    }
}
