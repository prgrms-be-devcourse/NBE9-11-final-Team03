package com.back.baton.domain.escrow.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.EscrowErrorCode;
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

    public static Escrow createHeld(Long tradeId, Long payerId, Long payeeId, Integer amount, LocalDateTime expiresAt) {
        Escrow escrow = new Escrow();
        escrow.tradeId = tradeId;
        escrow.payerId = payerId;
        escrow.payeeId = payeeId;
        escrow.amount = amount;
        escrow.fee = 0; // TODO: 중개 수수료 설정
        escrow.settlementAmount = amount; // TODO: 수수료 설정 시 반영
        escrow.status = EscrowStatus.HELD;
        escrow.expiresAt = expiresAt;
        return escrow;
    }
}