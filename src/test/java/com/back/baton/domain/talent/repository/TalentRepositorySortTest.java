package com.back.baton.domain.talent.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentSortType;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class TalentRepositorySortTest {

    @Autowired TalentRepository talentRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("평점순: avgRating DESC, 동점은 id DESC, 커서로 다음 페이지가 이어진다")
    void ratingSort_keysetPaging() {
        Category c = saveCategory();
        Talent t1 = save(c, dec(4.5), 0); // 동점 4.5, id 작음
        Talent t2 = save(c, dec(3.0), 0);
        Talent t3 = save(c, dec(4.5), 0); // 동점 4.5, id 큼
        Talent t4 = save(c, dec(5.0), 0);

        // 기대 정렬: t4(5.0) > t3(4.5,id큼) > t1(4.5,id작음) > t2(3.0)
        // size=2 -> size+1=3개 반환
        List<TalentListRes> page1 = talentRepository.findTalentList(null, 2, TalentSortType.RATING);
        assertThat(page1).extracting(TalentListRes::talentId)
                .containsExactly(t4.getId(), t3.getId(), t1.getId());

        // 2페이지: 커서 = 1페이지 마지막 노출 항목(t3) id
        List<TalentListRes> page2 = talentRepository.findTalentList(t3.getId(), 2, TalentSortType.RATING);
        assertThat(page2).extracting(TalentListRes::talentId)
                .containsExactly(t1.getId(), t2.getId());
    }

    @Test
    @DisplayName("인기순: completeCount DESC, 동점은 id DESC, 커서로 다음 페이지가 이어진다")
    void popularSort_keysetPaging() {
        Category c = saveCategory();
        Talent t1 = save(c, dec(0), 5); // 동점 5, id 작음
        Talent t2 = save(c, dec(0), 1);
        Talent t3 = save(c, dec(0), 5); // 동점 5, id 큼
        Talent t4 = save(c, dec(0), 9);

        // 기대 정렬: t4(9) > t3(5,id큼) > t1(5,id작음) > t2(1)
        List<TalentListRes> page1 = talentRepository.findTalentList(null, 2, TalentSortType.POPULAR);
        assertThat(page1).extracting(TalentListRes::talentId)
                .containsExactly(t4.getId(), t3.getId(), t1.getId());

        List<TalentListRes> page2 = talentRepository.findTalentList(t3.getId(), 2, TalentSortType.POPULAR);
        assertThat(page2).extracting(TalentListRes::talentId)
                .containsExactly(t1.getId(), t2.getId());
    }

    @Test
    @DisplayName("최신순: id DESC (기존 동작 유지)")
    void latestSort() {
        Category c = saveCategory();
        Talent t1 = save(c, dec(0), 0);
        Talent t2 = save(c, dec(0), 0);
        Talent t3 = save(c, dec(0), 0);

        List<TalentListRes> page = talentRepository.findTalentList(null, 10, TalentSortType.LATEST);
        assertThat(page).extracting(TalentListRes::talentId)
                .containsExactly(t3.getId(), t2.getId(), t1.getId());
    }

    private Category saveCategory() {
        try {
            Constructor<Category> ctor = Category.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            Category category = ctor.newInstance();
            ReflectionTestUtils.setField(category, "name", "백엔드");
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", true);
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
    }

    // avgRating, completeCount는 create에서 0 초기화되므로 reflection으로 세팅
    private Talent save(Category category, BigDecimal avgRating, int completeCount) {
        Talent talent = Talent.create(saveUserId(), category, "제목", "내용", 2, 100);
        ReflectionTestUtils.setField(talent, "avgRating", avgRating);
        ReflectionTestUtils.setField(talent, "completeCount", completeCount);
        return talentRepository.save(talent);
    }

    private Long saveUserId() {
        String suffix = String.valueOf(System.nanoTime());
        User user = User.builder()
                .email("author" + suffix + "@test.com")
                .password("password")
                .nickname("author" + suffix)
                .introduction("intro")
                .trustScore(BigDecimal.ZERO)
                .build();
        return userRepository.save(user).getId();
    }

    private BigDecimal dec(double v) {
        return BigDecimal.valueOf(v);
    }
}
