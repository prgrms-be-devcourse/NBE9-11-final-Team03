package com.back.baton.domain.credit.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "credit_account")
public class CreditAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // TODO: Users 연관관계 설정

    @Column(nullable = false)
    private int balance;

    @Column(nullable = false)
    private int escrowBalance;

    @Version
    private Long version;

}
