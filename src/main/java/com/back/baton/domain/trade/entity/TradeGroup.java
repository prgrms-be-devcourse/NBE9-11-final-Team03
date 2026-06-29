package com.back.baton.domain.trade.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trade_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_proposal_id", nullable = false, unique = true, updatable = false)
    private Long matchProposalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", length = 20, nullable = false, updatable = false)
    private TradeType tradeType;

    public static TradeGroup create(Long matchProposalId, TradeType tradeType) {
        TradeGroup group = new TradeGroup();
        group.matchProposalId = matchProposalId;
        group.tradeType = tradeType;
        return group;
    }
}