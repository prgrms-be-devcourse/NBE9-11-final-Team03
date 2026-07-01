package com.back.baton.domain.talent.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "talent_report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_talent_report_talent_reporter",
                columnNames = {"talent_id", "reporter_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TalentReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고 대상 재능
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "talent_id")
    private Talent talent;

    // 신고자
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private TalentReport(Talent talent, Long reporterId, ReportReason reason, String description) {
        this.talent = talent;
        this.reporterId = reporterId;
        this.reason = reason;
        this.description = description;
        this.status = ReportStatus.PENDING;   // 접수 시 항상 PENDING
    }

    public static TalentReport create(Talent talent, Long reporterId, ReportReason reason, String description) {
        return TalentReport.builder()
                .talent(talent)
                .reporterId(reporterId)
                .reason(reason)
                .description(description)
                .build();
    }

    public void resolve() {
        this.status = ReportStatus.RESOLVED;
    }
}
