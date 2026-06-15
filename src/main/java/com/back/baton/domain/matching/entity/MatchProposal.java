package com.back.baton.domain.matching.entity;

import com.back.baton.domain.matching.enums.MatchProposalStatus;
import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "match_proposals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchProposal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long providerTalentId;

    @Column(nullable = true)
    private Long requesterTalentId;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false)
    private Long providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchProposalStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestMessage;

    private LocalDateTime respondedAt;

    private MatchProposal(
            Long providerTalentId,
            Long requesterTalentId,
            Long requesterId,
            Long providerId,
            String requestMessage
    ) {
        this.providerTalentId = providerTalentId;
        this.requesterTalentId = requesterTalentId;
        this.requesterId = requesterId;
        this.providerId = providerId;
        this.requestMessage = requestMessage;
        this.status = MatchProposalStatus.REQUESTED;
    }

    public static MatchProposal create(
            Long providerTalentId,
            Long requesterTalentId,
            Long requesterId,
            Long providerId,
            String requestMessage
    ) {
        return new MatchProposal(
                providerTalentId,
                requesterTalentId,
                requesterId,
                providerId,
                requestMessage
        );
    }
}