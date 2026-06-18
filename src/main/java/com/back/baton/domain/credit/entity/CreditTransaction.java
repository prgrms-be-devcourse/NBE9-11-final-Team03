package com.back.baton.domain.credit.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "credit_transaction",
        indexes = @Index(name = "idx_credit_transaction_user_id", columnList = "user_id")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "related_trade_id", updatable = false)
    private Long relatedTradeId; // 초기 크레딧 지급 시에는 거래 ID가 없을 수 있음

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false, updatable = false)
    private CreditTransactionType type;

    @Column(name = "amount", nullable = false, updatable = false)
    private Integer amount; // 차감 시 음수, 적립 시 양수

    @Column(name = "balance_after", nullable = false, updatable = false)
    private Integer balanceAfter;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100, updatable = false)
    private String idempotencyKey; // 멱등성 키, 중복 요청 방지를 위한 unique 제약 조건

    @Column(name = "reason", length = 200, updatable = false)
    private String reason;

    public static CreditTransaction create(
            Long userId,
            Long relatedTradeId,
            CreditTransactionType type,
            Integer amount,
            Integer balanceAfter,
            String idempotencyKey,
            String reason
    ) {
        CreditTransaction ct = new CreditTransaction();
        ct.userId = userId;
        ct.relatedTradeId = relatedTradeId;
        ct.type = type;
        ct.amount = amount;
        ct.balanceAfter = balanceAfter;
        ct.idempotencyKey = idempotencyKey;
        ct.reason = reason;
        return ct;
    }
}