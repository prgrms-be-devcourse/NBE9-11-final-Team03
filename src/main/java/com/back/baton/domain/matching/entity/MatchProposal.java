package com.back.baton.domain.matching.entity;

import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.trade.entity.TradeType;
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

    @Column(nullable = false)
    private Integer providerTalentPriceSnapshot;

    private Integer requesterTalentPriceSnapshot;

    @Column(length = 100, unique = true)
    private String activeSwapPairKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestMessage;

    private LocalDateTime respondedAt;

    private MatchProposal(
            Long providerTalentId,
            Long requesterTalentId,
            Long requesterId,
            Long providerId,
            String requestMessage,
            Integer providerTalentPriceSnapshot,
            Integer requesterTalentPriceSnapshot,
            String activeSwapPairKey
    ) {
        this.providerTalentId = providerTalentId;
        this.requesterTalentId = requesterTalentId;
        this.requesterId = requesterId;
        this.providerId = providerId;
        this.status = MatchProposalStatus.REQUESTED;
        this.requestMessage = requestMessage;
        this.providerTalentPriceSnapshot = providerTalentPriceSnapshot;
        this.requesterTalentPriceSnapshot = requesterTalentPriceSnapshot;
        this.activeSwapPairKey = activeSwapPairKey;
    }

    public static MatchProposal create(
            Long providerTalentId,
            Long requesterTalentId,
            Long requesterId,
            Long providerId,
            String requestMessage,
            Integer providerTalentPriceSnapshot,
            Integer requesterTalentPriceSnapshot,
            String activeSwapPairKey
    ) {
        return new MatchProposal(
                providerTalentId,
                requesterTalentId,
                requesterId,
                providerId,
                requestMessage,
                providerTalentPriceSnapshot,
                requesterTalentPriceSnapshot,
                activeSwapPairKey
        );
    }

    public void accept() {
        this.status = MatchProposalStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = MatchProposalStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
        this.activeSwapPairKey = null;
    }

    public TradeType getTradeType() {
        return requesterTalentId == null ? TradeType.PURCHASE : TradeType.SWAP;
    }

    public boolean isSwap() {
        return getTradeType() == TradeType.SWAP;
    }

    public static MatchProposal createFromTalents(
            Talent providerTalent,
            Talent requesterTalent,
            Long requesterId,
            Long providerId,
            String requestMessage
    ) {
        Long requesterTalentId = null;
        Integer requesterTalentPriceSnapshot = null;
        String activeSwapPairKey = null;

        if (requesterTalent != null) {
            requesterTalentId = requesterTalent.getId();
            requesterTalentPriceSnapshot = requesterTalent.getCreditPrice();
            activeSwapPairKey = createActiveSwapPairKey(
                    requesterTalent.getId(),
                    providerTalent.getId()
            );
        }

        return new MatchProposal(
                providerTalent.getId(),
                requesterTalentId,
                requesterId,
                providerId,
                requestMessage,
                providerTalent.getCreditPrice(),
                requesterTalentPriceSnapshot,
                activeSwapPairKey
        );
    }

    public static String createActiveSwapPairKey(Long requesterTalentId, Long providerTalentId) {
        if (requesterTalentId == null || providerTalentId == null) {
            return null;
        }

        long min = Math.min(requesterTalentId, providerTalentId);
        long max = Math.max(requesterTalentId, providerTalentId);

        return min + ":" + max;
    }
}
