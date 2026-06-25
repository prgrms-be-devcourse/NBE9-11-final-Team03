package com.back.baton.domain.user.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RefreshToken extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId; // 연관관계 매핑 X

    @Column(nullable = false, length = 512)
    private String tokenValue;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    public RefreshToken(Long userId, String tokenValue, LocalDateTime expiredAt){
        this.userId = userId;
        this.tokenValue = tokenValue;
        this.expiredAt = expiredAt;
    }

    public void update(String tokenValue, LocalDateTime expiredAt){
        this.tokenValue = tokenValue;
        this.expiredAt = expiredAt;
    }
}
