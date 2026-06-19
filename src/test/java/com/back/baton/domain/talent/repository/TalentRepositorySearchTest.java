package com.back.baton.domain.talent.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.entity.Talent;
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
class TalentRepositorySearchTest {

    @Autowired TalentRepository talentRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    @DisplayName("필터 없으면 삭제 제외 전체를 최신순으로 조회한다")
    void search_noFilter() {
        Category c = saveCategory("백엔드", true);
        save(c, "재능1", 100, dec(0), 0);
        save(c, "재능2", 200, dec(0), 0);
        Talent deleted = save(c, "삭제됨", 300, dec(0), 0);
        deleted.softDelete();
        talentRepository.save(deleted);

        var req = new TalentSearchReq(null, null, null, null, null);
        List<TalentListRes> result = talentRepository.searchTalents(req, null, 10);

        assertThat(result).extracting(TalentListRes::title)
                .containsExactly("재능2", "재능1"); // 최신순, 삭제 제외
    }

    @Test
    @DisplayName("카테고리로 필터링한다")
    void search_byCategory() {
        Category backend = saveCategory("백엔드", true);
        Category design = saveCategory("디자인", true);
        save(backend, "스프링", 100, dec(0), 0);
        save(design, "피그마", 100, dec(0), 0);

        var req = new TalentSearchReq(backend.getId(), null, null, null, null);
        List<TalentListRes> result = talentRepository.searchTalents(req, null, 10);

        assertThat(result).extracting(TalentListRes::title).containsExactly("스프링");
        assertThat(result).extracting(TalentListRes::categoryName).containsOnly("백엔드");
    }

    @Test
    @DisplayName("크레딧 범위(min~max)로 필터링한다")
    void search_byCreditRange() {
        Category c = saveCategory("백엔드", true);
        save(c, "싼거", 50, dec(0), 0);
        save(c, "중간", 200, dec(0), 0);
        save(c, "비싼거", 500, dec(0), 0);

        var req = new TalentSearchReq(null, 100, 300, null, null);
        List<TalentListRes> result = talentRepository.searchTalents(req, null, 10);

        assertThat(result).extracting(TalentListRes::title).containsExactly("중간");
    }

    @Test
    @DisplayName("최소 평점 이상만 필터링한다")
    void search_byMinRating() {
        Category c = saveCategory("백엔드", true);
        save(c, "별로", 100, dec(3.0), 0);
        save(c, "좋음", 100, dec(4.5), 0);

        var req = new TalentSearchReq(null, null, null, dec(4.0), null);
        List<TalentListRes> result = talentRepository.searchTalents(req, null, 10);

        assertThat(result).extracting(TalentListRes::title).containsExactly("좋음");
    }

    @Test
    @DisplayName("completedOnly=true면 완료 1건 이상만, false/null이면 전체")
    void search_byCompleted() {
        Category c = saveCategory("백엔드", true);
        save(c, "신규", 100, dec(0), 0);     // completeCount 0
        save(c, "거래있음", 100, dec(0), 3);  // completeCount 3

        // true: 완료 1건 이상만
        var reqOnly = new TalentSearchReq(null, null, null, null, true);
        assertThat(talentRepository.searchTalents(reqOnly, null, 10))
                .extracting(TalentListRes::title).containsExactly("거래있음");

        // false: 필터 안 걸림(전체)
        var reqFalse = new TalentSearchReq(null, null, null, null, false);
        assertThat(talentRepository.searchTalents(reqFalse, null, 10)).hasSize(2);

        // null: 필터 안 걸림(전체)
        var reqNull = new TalentSearchReq(null, null, null, null, null);
        assertThat(talentRepository.searchTalents(reqNull, null, 10)).hasSize(2);
    }

    @Test
    @DisplayName("복합 조건(카테고리+크레딧+완료)을 동시에 적용한다")
    void search_combined() {
        Category backend = saveCategory("백엔드", true);
        Category design = saveCategory("디자인", true);
        save(backend, "정답", 200, dec(0), 5);    // 모든 조건 충족
        save(backend, "완료없음", 200, dec(0), 0);  // 완료 탈락
        save(backend, "비쌈", 999, dec(0), 5);      // 크레딧 탈락
        save(design, "다른카테", 200, dec(0), 5);   // 카테고리 탈락

        var req = new TalentSearchReq(backend.getId(), 100, 300, null, true);
        List<TalentListRes> result = talentRepository.searchTalents(req, null, 10);

        assertThat(result).extracting(TalentListRes::title).containsExactly("정답");
    }

    @Test
    @DisplayName("커서 이후(id < cursor)만 + 필터 동시 적용한다")
    void search_withCursor() {
        Category c = saveCategory("백엔드", true);
        Talent t1 = save(c, "재능1", 100, dec(0), 0);
        Talent t2 = save(c, "재능2", 100, dec(0), 0);
        Talent t3 = save(c, "재능3", 100, dec(0), 0);

        var req = new TalentSearchReq(c.getId(), null, null, null, null);
        List<TalentListRes> result = talentRepository.searchTalents(req, t3.getId(), 10);

        assertThat(result).extracting(TalentListRes::talentId)
                .containsExactly(t2.getId(), t1.getId()); // t3 미만
    }

    private Category saveCategory(String name, boolean active) {
        try {
            Constructor<Category> ctor = Category.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            Category category = ctor.newInstance();
            ReflectionTestUtils.setField(category, "name", name);
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", active);
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
    }

    // avgRating, completeCount는 Talent.create에서 0으로 초기화되므로 reflection으로 세팅
    private Talent save(Category category, String title, int creditPrice, BigDecimal avgRating, int completeCount) {
        Talent talent = Talent.create(1L, category, title, "내용", 2, creditPrice);
        ReflectionTestUtils.setField(talent, "avgRating", avgRating);
        ReflectionTestUtils.setField(talent, "completeCount", completeCount);
        return talentRepository.save(talent);
    }

    private BigDecimal dec(double v) {
        return BigDecimal.valueOf(v);
    }
}