package com.back.baton.global.initData;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.profile.entity.Profile;
import com.back.baton.domain.profile.repository.ProfileRepository;
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
    @Autowired
    private ProfileRepository profileRepository;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            self.setCategory();
            self.setUser();
            self.setProfile();
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
            authService.signup("user"+i+"@test.com", "password1234!", "user"+i,
                    "재능 교환 및 에스크로 테스트용 사용자 " + i, null);
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

    private static final List<ProfileSeedInfo> PROFILE_SEEDS = List.of(
            new ProfileSeedInfo(1, List.of("디자인")),
            new ProfileSeedInfo(2, List.of("개발")),
            new ProfileSeedInfo(3, List.of("디자인")),
            new ProfileSeedInfo(4, List.of("문서정리")),
            new ProfileSeedInfo(5, List.of("개발")),
            new ProfileSeedInfo(6, List.of("개발")),
            new ProfileSeedInfo(7, List.of("개발")),
            new ProfileSeedInfo(8, List.of("디자인")),
            new ProfileSeedInfo(9, List.of("개발")),
            new ProfileSeedInfo(10, List.of("문서정리"))
    );

    @Transactional
    public void setProfile() {
        Map<String, Category> categoryMap = getCategoryMap();

        for (ProfileSeedInfo seed : PROFILE_SEEDS) {
            User user = getUser(seed.userIndex());
            Profile profile = profileRepository.findByUserId(user.getId()).orElseThrow();
            profile.update(
                    null,
                    getCategories(categoryMap, seed.wantTalentCategoryNames()),
                    null
            );
        }
    }

    private static final List<TalentSeedInfo> TALENT_SEEDS = List.of(
            new TalentSeedInfo(1, "개발", "Python으로 엑셀 자동화 스크립트 만들어드립니다",
                    "반복적인 엑셀 작업을 Python으로 자동화해드립니다. 데이터 정리, 보고서 생성, 이메일 발송 자동화 등 요청사항에 맞게 제작합니다.", 3, 150),
            new TalentSeedInfo(1, "문서정리", "API 테스트 체크리스트 정리해드립니다",
                    "기능 테스트에 필요한 API 요청 순서, 검증 포인트, 예상 응답을 보기 쉬운 체크리스트로 정리합니다.", 2, 90),
            new TalentSeedInfo(2, "디자인", "PPT 발표 자료 디자인해드립니다",
                    "깔끔하고 전문적인 발표 자료를 제작해드립니다. 취업, 사업계획서, 학교 발표 등 목적에 맞는 슬라이드 디자인을 제공합니다.", 4, 200),
            new TalentSeedInfo(2, "문서정리", "회의록 및 액션 아이템 정리합니다",
                    "회의 녹취나 메모를 기반으로 결정 사항, 담당자, 마감일을 포함한 회의록을 작성합니다.", 2, 80),
            new TalentSeedInfo(3, "문서정리", "회의록 및 업무 보고서 작성 도와드립니다",
                    "회의 내용을 체계적으로 정리하고 읽기 쉬운 보고서 형태로 작성해드립니다. Notion, Word, 구글 독스 등 원하시는 포맷으로 제공합니다.", 2, 80),
            new TalentSeedInfo(3, "개발", "간단한 랜딩 페이지 퍼블리싱 도와드립니다",
                    "정적인 소개 페이지나 이벤트 페이지를 HTML, CSS 기반으로 빠르게 구현합니다.", 5, 220),
            new TalentSeedInfo(4, "디자인", "인스타그램 카드뉴스 및 썸네일 제작",
                    "브랜드 톤에 맞는 SNS 콘텐츠 카드뉴스와 유튜브 썸네일을 제작해드립니다. Canva 또는 Photoshop으로 작업합니다.", 3, 120),
            new TalentSeedInfo(4, "개발", "간단한 웹 폼 기능 구현합니다",
                    "문의 폼, 신청 폼, 입력 검증 등 작은 웹 기능 구현을 도와드립니다.", 4, 180),
            new TalentSeedInfo(5, "문서정리", "영어 이메일 교정 및 번역해드립니다",
                    "비즈니스 영어 이메일 작성, 문서 번역, 영작 교정을 도와드립니다. 자연스럽고 격식에 맞는 표현으로 다듬어드립니다.", 2, 100),
            new TalentSeedInfo(5, "디자인", "간단한 배너 이미지 제작합니다",
                    "공지, 이벤트, 프로모션에 사용할 웹 배너 이미지를 목적에 맞게 제작합니다.", 2, 110),
            new TalentSeedInfo(6, "문서정리", "리서치 자료를 요약 보고서로 정리합니다",
                    "시장 조사 자료, 인터뷰 메모, 논문 초안을 핵심만 남겨 읽기 쉬운 요약 보고서로 정리합니다.", 5, 180),
            new TalentSeedInfo(6, "디자인", "Notion 페이지 커버와 아이콘을 제작합니다",
                    "프로젝트나 포트폴리오 Notion 페이지에 어울리는 커버 이미지와 아이콘을 제작합니다.", 2, 90),
            new TalentSeedInfo(7, "디자인", "서비스 소개 랜딩 페이지 시안을 제작합니다",
                    "초기 스타트업 또는 사이드 프로젝트의 서비스 소개 화면을 와이어프레임부터 시각 시안까지 제작합니다.", 6, 260),
            new TalentSeedInfo(7, "문서정리", "포트폴리오 문구를 다듬어드립니다",
                    "프로젝트 설명, 역할, 성과가 잘 보이도록 포트폴리오 문구를 정리하고 교정합니다.", 3, 130),
            new TalentSeedInfo(8, "개발", "Spring Boot API 기능 구현을 도와드립니다",
                    "간단한 CRUD, 인증 연동, 관리자 API 등 Spring Boot 기반 백엔드 기능 구현과 리팩터링을 지원합니다.", 8, 350),
            new TalentSeedInfo(8, "문서정리", "기능 명세서를 정리합니다",
                    "기획 메모를 바탕으로 사용자 흐름, 정책, 예외 케이스가 포함된 기능 명세서를 작성합니다.", 4, 170),
            new TalentSeedInfo(9, "문서정리", "API 명세와 테스트 체크리스트를 정리합니다",
                    "개발 중인 기능의 API 명세, 요청/응답 예시, QA 체크리스트를 팀원이 바로 볼 수 있게 정리합니다.", 4, 160),
            new TalentSeedInfo(9, "디자인", "와이어프레임을 깔끔하게 정리합니다",
                    "손그림이나 메모 수준의 화면 구성을 공유 가능한 와이어프레임으로 정리합니다.", 4, 190),
            new TalentSeedInfo(10, "디자인", "모바일 앱 화면 UI를 정리해드립니다",
                    "앱 주요 화면의 정보 구조와 UI 스타일을 정리하고 개발자가 참고할 수 있는 화면 단위 산출물을 제공합니다.", 5, 240),
            new TalentSeedInfo(10, "개발", "간단한 관리자 페이지 화면을 구현합니다",
                    "목록, 검색, 상세 확인 중심의 간단한 관리자 화면 구현을 도와드립니다.", 6, 280)
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

    private User getUser(int userIndex) {
        return userRepository.findByEmail("user" + userIndex + "@test.com").orElseThrow();
    }

    private Map<String, Category> getCategoryMap() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getName, c -> c));
    }

    private List<Category> getCategories(Map<String, Category> categoryMap, List<String> categoryNames) {
        return categoryNames.stream()
                .map(categoryMap::get)
                .toList();
    }

    private record SeedInfo(String name, int sortOrder) {}
    private record ProfileSeedInfo(int userIndex, List<String> wantTalentCategoryNames) {}
    private record TalentSeedInfo(int userIndex, String categoryName, String title, String content, int estimatedHours, int creditPrice) {}
}
