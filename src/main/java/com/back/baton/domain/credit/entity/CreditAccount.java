package com.back.baton.domain.credit.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "credit_account")
@Check(constraints = "balance >= 0 AND escrow_balance >= 0")
public class CreditAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int balance;

    @Column(nullable = false)
    private int escrowBalance;

    @Version
    private Long version;

    private LocalDateTime deletedAt;

    public static CreditAccount create(Long userId, int initialBalance) {
        CreditAccount account = new CreditAccount();
        account.userId = userId;
        account.balance = initialBalance;
        account.escrowBalance = 0; // 계좌 생성 시 에스크로 잔액은 0으로 초기화
        return account;
    }

}
