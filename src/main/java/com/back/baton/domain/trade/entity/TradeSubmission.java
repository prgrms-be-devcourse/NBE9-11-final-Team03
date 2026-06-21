package com.back.baton.domain.trade.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeSubmission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "escrow_id", nullable = false, updatable = false, unique = true)
    private Long escrowId;

    @Column(name = "file_key", nullable = false, updatable = false, length = 200)
    private String fileKey;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    public static TradeSubmission create(Long escrowId, String fileKey, String description) {
        TradeSubmission submission = new TradeSubmission();
        submission.escrowId = escrowId;
        submission.fileKey = fileKey;
        submission.description = description;
        submission.submittedAt = LocalDateTime.now();
        return submission;
    }
}