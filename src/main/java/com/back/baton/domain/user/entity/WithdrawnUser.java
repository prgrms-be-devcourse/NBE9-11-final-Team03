package com.back.baton.domain.user.entity;

import com.back.baton.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {@Index(name = "idx_encoded_email", columnList = "encodedEmail")})
public class WithdrawnUser extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    String encodedEmail;

    @Column(nullable = false)
    boolean permanentBan = false; // 영구정지 여부

    public WithdrawnUser(String encodedEmail, UserStatus status){
        this.encodedEmail = encodedEmail;
        if(status == UserStatus.BANNED){
            this.permanentBan = true;
        }
    }
}
