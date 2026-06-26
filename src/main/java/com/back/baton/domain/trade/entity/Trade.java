package com.back.baton.domain.trade.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_group_id", nullable = false, updatable = false)
    private Long tradeGroupId;

    @Column(name = "talent_id", nullable = false)
    private Long talentId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "credit_price", nullable = false)
    private Integer creditPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TradeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", length = 20, nullable = false)
    private TradeType tradeType;

    public static Trade create(Long tradeGroupId, Long talentId, Long buyerId, Long sellerId, Integer creditPrice, TradeType tradeType) {
        Trade trade = new Trade();
        trade.tradeGroupId = tradeGroupId;
        trade.talentId = talentId;
        trade.buyerId = buyerId;
        trade.sellerId = sellerId;
        trade.creditPrice = creditPrice;
        trade.status = TradeStatus.IN_PROGRESS;
        trade.tradeType = tradeType;
        return trade;
    }

    public void cancel() {
        this.status = TradeStatus.CANCELLED;
    }

    public void submitResult() {
        this.status = TradeStatus.UNDER_REVIEW;
    }

    public void complete() {
        this.status = TradeStatus.COMPLETED;
    }

    public void dispute() {
        this.status = TradeStatus.DISPUTED;
    }
}