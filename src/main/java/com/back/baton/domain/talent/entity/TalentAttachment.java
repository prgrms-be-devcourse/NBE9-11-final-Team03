package com.back.baton.domain.talent.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "talent_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TalentAttachment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "talent_id")
    private Talent talent;

    // S3에 업로드된 객체의 key 또는 외부 참고 링크 URL
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    // 링크 설명
    @Column(name = "description", length = 200)
    private String description;

    @Builder(access = AccessLevel.PRIVATE)
    private TalentAttachment(Talent talent, String url, String description) {
        this.talent = talent;
        this.url = url;
        this.description = description;
    }

    public static TalentAttachment create(Talent talent, String url, String description) {
        return TalentAttachment.builder()
                .talent(talent)
                .url(url)
                .description(description)
                .build();
    }
}