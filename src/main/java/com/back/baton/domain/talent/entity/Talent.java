package com.back.baton.domain.talent.entity;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "talent")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Talent extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "estimated_hours", nullable = false)
    private Integer estimatedHours; //예상 소요 시간

    @Column(name = "credit_price", nullable = false)
    private Integer creditPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TalentStatus status;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "complete_count", nullable = false)
    private int completeCount;

    @Column(name = "avg_rating", precision = 3, scale = 2, nullable = false)
    private BigDecimal avgRating;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Talent(Long authorId, Category category, String title, String content, Integer estimatedHours,
                   Integer creditPrice) {
        this.authorId = authorId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.estimatedHours = estimatedHours;
        this.creditPrice = creditPrice;
        this.status = TalentStatus.ACTIVE;
        this.viewCount = 0;
        this.completeCount = 0;
        this.avgRating = BigDecimal.ZERO;
    }

    public static Talent create(Long authorId, Category category, String title, String content, Integer estimatedHours,
                                Integer creditPrice){
        return Talent.builder()
                .authorId(authorId).category(category)
                .title(title).content(content)
                .estimatedHours(estimatedHours).creditPrice(creditPrice).build();
    }
}