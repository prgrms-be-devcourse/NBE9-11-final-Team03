package com.back.baton.global.initData;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.domain.user.service.AuthService;
import com.back.baton.domain.user.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TalentRepository talentRepository;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            self.setCategory();
            self.setUser();
            self.setTalent();
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
            emailVerificationService.markVerifiedForTrustedEmail("user"+i+"@test.com");
            authService.signup("user"+i+"@test.com", "password1234!", "user"+i, "간단한 설명",null);
        }
    }

    // 순수 데이터만 static 보관
    private static final List<SeedInfo> CATEGORY_SEEDS = List.of(
            new SeedInfo("개발", 1),
            new SeedInfo("디자인", 2),
            new SeedInfo("문서정리", 3)
    );

    @Transactional
    public void setCategory() {
        for (SeedInfo seed : CATEGORY_SEEDS) {
            if (!categoryRepository.existsByName(seed.name())) {
                categoryRepository.save(Category.create(seed.name(), seed.sortOrder()));
            }
        }
    }

    private static final List<TalentSeedInfo> TALENT_SEEDS = List.of(
            new TalentSeedInfo(1, "개발", "Python으로 엑셀 자동화 스크립트 만들어드립니다",
                    "반복적인 엑셀 작업을 Python으로 자동화해드립니다. 데이터 정리, 보고서 생성, 이메일 발송 자동화 등 요청사항에 맞게 제작합니다.", 3, 150),
            new TalentSeedInfo(2, "디자인", "PPT 발표 자료 디자인해드립니다",
                    "깔끔하고 전문적인 발표 자료를 제작해드립니다. 취업, 사업계획서, 학교 발표 등 목적에 맞는 슬라이드 디자인을 제공합니다.", 4, 200),
            new TalentSeedInfo(3, "문서정리", "회의록 및 업무 보고서 작성 도와드립니다",
                    "회의 내용을 체계적으로 정리하고 읽기 쉬운 보고서 형태로 작성해드립니다. Notion, Word, 구글 독스 등 원하시는 포맷으로 제공합니다.", 2, 80),
            new TalentSeedInfo(4, "디자인", "인스타그램 카드뉴스 및 썸네일 제작",
                    "브랜드 톤에 맞는 SNS 콘텐츠 카드뉴스와 유튜브 썸네일을 제작해드립니다. Canva 또는 Photoshop으로 작업합니다.", 3, 120),
            new TalentSeedInfo(5, "문서정리", "영어 이메일 교정 및 번역해드립니다",
                    "비즈니스 영어 이메일 작성, 문서 번역, 영작 교정을 도와드립니다. 자연스럽고 격식에 맞는 표현으로 다듬어드립니다.", 2, 100)
    );

    @Transactional
    public void setTalent() {
        if (talentRepository.count() > 0) return;

        Map<String, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getName, c -> c));

        for (TalentSeedInfo seed : TALENT_SEEDS) {
            User user = userRepository.findByEmail("user" + seed.userIndex() + "@test.com").orElseThrow();
            Category category = categoryMap.get(seed.categoryName());
            talentRepository.save(Talent.create(user.getId(), category, seed.title(), seed.content(),
                    seed.estimatedHours(), seed.creditPrice()));
        }
    }

    private record SeedInfo(String name, int sortOrder) {}
    private record TalentSeedInfo(int userIndex, String categoryName, String title, String content, int estimatedHours, int creditPrice) {}
}
