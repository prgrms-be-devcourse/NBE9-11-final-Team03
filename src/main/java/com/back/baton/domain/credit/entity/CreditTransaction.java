package com.back.baton.domain.credit.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Schema(description = "크레딧 거래 원장 엔티티")
@Entity
@Table(
        name = "credit_transaction",
        indexes = {
                @Index(name = "idx_credit_transaction_user_id", columnList = "user_id"),
                @Index(name = "idx_credit_transaction_related_trade_id", columnList = "related_trade_id")
        }
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

    @Schema(description = "크레딧 거래 유형", example = "ESCROW_HOLD")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false, updatable = false)
    private CreditTransactionType type;

    @Schema(description = "거래할 금액. 차감 시 음수/적립 시 양수", example = "-5000")
    @Column(name = "amount", nullable = false, updatable = false)
    private Integer amount;

    @Schema(description = "거래 후 잔액", example = "15000")
    @Column(name = "balance_after", nullable = false, updatable = false)
    private Integer balanceAfter;

    @Schema(description = "거래 유형별 고정 기본 사유 (type.defaultReason 스냅샷)", example = "거래 완료까지 크레딧 에스크로 예치")
    @Column(name = "default_reason", length = 200, nullable = false, updatable = false)
    private String defaultReason;

    @Schema(description = "건별 실제 사유. 관리자 수동 조정 등에만 사용하며 일반 거래에서는 null", example = "환불 지연으로 50 보상", nullable = true)
    @Column(name = "detail_reason", length = 200, updatable = false)
    private String detailReason;

    public static CreditTransaction create(
            Long userId,
            Long relatedTradeId,
            CreditTransactionType type,
            Integer amount,
            Integer balanceAfter,
            String detailReason
    ) {
        CreditTransaction ct = new CreditTransaction();
        ct.userId = userId;
        ct.relatedTradeId = relatedTradeId;
        ct.type = type;
        ct.amount = amount;
        ct.balanceAfter = balanceAfter;
        ct.defaultReason = type.getDefaultReason();
        ct.detailReason = detailReason;
        return ct;
    }
}