package com.back.baton.global.initData;

import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {

    @Autowired
    @Lazy
    private BaseInitData self;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            self.setUser();
        };
    }
    @Transactional
    public void setUser() {

        if (userRepository.count() > 0) return;

        // 테스트용 관리자 계정 생성
        User admin = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin1234!")) // 실시간 해싱 처리 가능!
                .nickname("admin")
                .introduction("최고 관리자 계정입니다.")
                .trustScore(new BigDecimal("100.00"))
                .build();
        admin.upgradeToAdmin();
        userRepository.save(admin);

        // 테스트용 유저 10명
        for(int i=1; i<=10; i++){
            authService.signup("user"+i+"@test.com", "password1234!", "user"+i, "간단한 설명",null);
        }
    }
}
