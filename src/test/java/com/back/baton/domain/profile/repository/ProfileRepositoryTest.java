package com.back.baton.domain.profile.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.profile.entity.Profile;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save and findById persist a profile with its user")
    void saveAndFindById() {
        User user = saveUser("user1@test.com", "user1");
        Profile savedProfile = profileRepository.save(new Profile(user));

        Optional<Profile> result = profileRepository.findById(savedProfile.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedProfile.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().isVisible()).isTrue();
    }

    @Test
    @DisplayName("findByUserId는 다른 user의 프로필을 가져오지 않는다.")
    void findByUserId() {
        User targetUser = saveUser("target@test.com", "target");
        User otherUser = saveUser("other@test.com", "other");

        Profile targetProfile = profileRepository.save(new Profile(targetUser));
        profileRepository.save(new Profile(otherUser));

        Optional<Profile> result = profileRepository.findByUserId(targetUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(targetProfile.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(targetUser.getId());
    }

    @Test
    @DisplayName("프로필이 없을 때 findByUserId returns empty")
    void findByUserId_empty() {
        User user = saveUser("no-profile@test.com", "noProfile");

        Optional<Profile> result = profileRepository.findByUserId(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findWithUserByUserId는 유저 정보와 함께 profile을 가져온다")
    void findWithUserByUserId() {
        User user = saveUser("with-user@test.com", "withUser");
        Profile profile = profileRepository.save(new Profile(user));
        flushAndClear();

        Optional<Profile> result = profileRepository.findWithUserByUserId(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(profile.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getUser().getNickname()).isEqualTo("withUser");
    }

    @Test
    @DisplayName("findWithUserByUserId returns empty when the user has no profile")
    void findWithUserByUserId_empty() {
        User user = saveUser("with-user-empty@test.com", "withUserEmpty");
        flushAndClear();

        Optional<Profile> result = profileRepository.findWithUserByUserId(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findPortfolioLinksByUserId returns only the user's portfolio links")
    void findPortfolioLinksByUserId() {
        User targetUser = saveUser("portfolio-target@test.com", "portfolioTarget");
        User otherUser = saveUser("portfolio-other@test.com", "portfolioOther");

        Profile targetProfile = new Profile(targetUser);
        targetProfile.update(null, null, List.of(
                "https://example.com/target-1",
                "https://example.com/target-2"
        ));
        profileRepository.save(targetProfile);

        Profile otherProfile = new Profile(otherUser);
        otherProfile.update(null, null, List.of("https://example.com/other"));
        profileRepository.save(otherProfile);
        flushAndClear();

        List<String> result = profileRepository.findPortfolioLinksByUserId(targetUser.getId());

        assertThat(result)
                .containsExactlyInAnyOrder(
                        "https://example.com/target-1",
                        "https://example.com/target-2"
                );
    }

    @Test
    @DisplayName("findPortfolioLinksByUserId returns an empty list when no links exist")
    void findPortfolioLinksByUserId_empty() {
        User user = saveUser("portfolio-empty@test.com", "portfolioEmpty");
        profileRepository.save(new Profile(user));
        flushAndClear();

        List<String> result = profileRepository.findPortfolioLinksByUserId(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findMyTalentCategoriesByUserId returns my talent categories ordered by sortOrder")
    void findMyTalentCategoriesByUserId() {
        User user = saveUser("my-category@test.com", "myCategory");
        Category backend = categoryRepository.save(Category.create("Backend", 2));
        Category design = categoryRepository.save(Category.create("Design", 1));
        Category planning = categoryRepository.save(Category.create("Planning", 3));

        Profile profile = new Profile(user);
        profile.update(List.of(backend, planning, design), null, null);
        profileRepository.save(profile);
        flushAndClear();

        List<Category> result = profileRepository.findMyTalentCategoriesByUserId(user.getId());

        assertThat(result)
                .extracting(Category::getName)
                .containsExactly("Design", "Backend", "Planning");
    }

    @Test
    @DisplayName("findWantTalentCategoriesByUserId returns wanted talent categories ordered by sortOrder")
    void findWantTalentCategoriesByUserId() {
        User user = saveUser("want-category@test.com", "wantCategory");
        Category backend = categoryRepository.save(Category.create("Backend", 2));
        Category design = categoryRepository.save(Category.create("Design", 1));
        Category planning = categoryRepository.save(Category.create("Planning", 3));

        Profile profile = new Profile(user);
        profile.update(null, List.of(planning, backend, design), null);
        profileRepository.save(profile);
        flushAndClear();

        List<Category> result = profileRepository.findWantTalentCategoriesByUserId(user.getId());

        assertThat(result)
                .extracting(Category::getName)
                .containsExactly("Design", "Backend", "Planning");
    }

    @Test
    @DisplayName("findAll, count, and existsById reflect saved profiles")
    void findAllCountAndExistsById() {
        Profile firstProfile = profileRepository.save(new Profile(saveUser("first@test.com", "first")));
        Profile secondProfile = profileRepository.save(new Profile(saveUser("second@test.com", "second")));

        List<Profile> result = profileRepository.findAll();

        assertThat(result)
                .extracting(Profile::getId)
                .containsExactlyInAnyOrder(firstProfile.getId(), secondProfile.getId());
        assertThat(profileRepository.count()).isEqualTo(2);
        assertThat(profileRepository.existsById(firstProfile.getId())).isTrue();
        assertThat(profileRepository.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("profile updates persist talent categories, portfolio links, and visibility")
    void update() {
        User user = saveUser("update@test.com", "updateUser");
        Category backend = categoryRepository.save(Category.create("Backend", 1));
        Category design = categoryRepository.save(Category.create("Design", 2));
        Category planning = categoryRepository.save(Category.create("Planning", 3));

        Profile profile = profileRepository.save(new Profile(user));
        profile.update(
                List.of(backend, design),
                List.of(planning),
                List.of("https://example.com/portfolio")
        );
        profile.setVisible(false);
        profileRepository.flush();

        Profile result = profileRepository.findById(profile.getId()).orElseThrow();

        assertThat(result.getMyTalentCategories())
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Backend", "Design");
        assertThat(result.getWantTalentCategories())
                .extracting(Category::getName)
                .containsExactly("Planning");
        assertThat(result.getPortfolioLinkList())
                .containsExactly("https://example.com/portfolio");
        assertThat(result.isVisible()).isFalse();
    }

    @Test
    @DisplayName("delete removes the saved profile")
    void delete() {
        Profile profile = profileRepository.save(new Profile(saveUser("delete@test.com", "deleteUser")));

        profileRepository.delete(profile);
        profileRepository.flush();

        assertThat(profileRepository.findById(profile.getId())).isEmpty();
        assertThat(profileRepository.existsById(profile.getId())).isFalse();
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password("password")
                        .nickname(nickname)
                        .profileImageUrl(null)
                        .introduction("test introduction")
                        .trustScore(BigDecimal.ZERO)
                        .build()
        );
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
