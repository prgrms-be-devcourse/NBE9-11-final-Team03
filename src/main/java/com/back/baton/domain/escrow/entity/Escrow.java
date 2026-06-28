package com.back.baton.domain.escrow.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "escrow",
        indexes = {
                @Index(name = "idx_escrow_payer", columnList = "payer_id"),
                @Index(name = "idx_escrow_payee", columnList = "payee_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Escrow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true, updatable = false)
    private Long tradeId;

    @Column(name = "payer_id", nullable = false, updatable = false)
    private Long payerId;

    @Column(name = "payee_id", nullable = false, updatable = false)
    private Long payeeId;

    @Column(name = "amount", nullable = false, updatable = false)
    private Integer amount;

    @Column(name = "fee", nullable = false, updatable = false)
    private Integer fee;

    @Column(name = "settlement_amount", nullable = false, updatable = false)
    private Integer settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private EscrowStatus status;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "review_requested_at")
    private LocalDateTime reviewRequestedAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    public void refund() {
        if (this.status != EscrowStatus.HELD) {
            throw new CustomException(EscrowErrorCode.INVALID_ESCROW_STATUS);
        }
        this.status = EscrowStatus.REFUNDED;
        this.settledAt = LocalDateTime.now();
    }

    public void release() {
        if (this.status != EscrowStatus.HELD) {
            throw new CustomException(EscrowErrorCode.INVALID_ESCROW_STATUS);
        }
        this.status = EscrowStatus.RELEASED;
        this.settledAt = LocalDateTime.now();
    }

    public void refundFrozen() {
        if (this.status != EscrowStatus.FROZEN) {
            throw new CustomException(EscrowErrorCode.INVALID_ESCROW_STATUS);
        }
        this.status = EscrowStatus.REFUNDED;
        this.settledAt = LocalDateTime.now();
    }

    public void releaseFrozen() {
        if (this.status != EscrowStatus.FROZEN) {
            throw new CustomException(EscrowErrorCode.INVALID_ESCROW_STATUS);
        }
        this.status = EscrowStatus.RELEASED;
        this.settledAt = LocalDateTime.now();
    }

    public void freeze(String reason) {
        if (this.status != EscrowStatus.HELD) {
            throw new CustomException(EscrowErrorCode.INVALID_ESCROW_STATUS);
        }
        // 분쟁 사유 검증
        if (reason == null || reason.isBlank() || reason.length() < 5 || reason.length() > 200) {
            throw new CustomException(TradeErrorCode.INVALID_DISPUTE_REASON);
        }
        this.status = EscrowStatus.FROZEN;
        this.rejectReason = reason;
        this.expiresAt = null; // 분쟁 발생 시 자동 확정 타이머 정지
    }

    // 에스크로 내 만료 시간 설정
    public void startReviewPeriod(LocalDateTime reviewRequestedAt, LocalDateTime expiresAt) {
        if (this.status != EscrowStatus.HELD) {
            throw new CustomException(EscrowErrorCode.INVALID_ESCROW_STATUS);
        }
        this.reviewRequestedAt = reviewRequestedAt;
        this.expiresAt = expiresAt;
    }

    public static Escrow createHeld(Long tradeId, Long payerId, Long payeeId, Integer amount, Integer fee, Integer settlementAmount, LocalDateTime expiresAt) {
        Escrow escrow = new Escrow();
        escrow.tradeId = tradeId;
        escrow.payerId = payerId;
        escrow.payeeId = payeeId;
        escrow.amount = amount;
        escrow.fee = fee;
        escrow.settlementAmount = settlementAmount;
        escrow.status = EscrowStatus.HELD;
        escrow.expiresAt = expiresAt;
        return escrow;
    }
}