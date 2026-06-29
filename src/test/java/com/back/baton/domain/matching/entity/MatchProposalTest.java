package com.back.baton.domain.matching.entity;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.trade.entity.TradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class MatchProposalTest {

    @Test
    @DisplayName("재능 교환 제안 생성 시 REQUESTED 상태이다.")
    void createMatchProposal() {
        MatchProposal matchProposal = createSwapProposal();

        assertThat(matchProposal.getProviderTalentId()).isEqualTo(20L);
        assertThat(matchProposal.getRequesterTalentId()).isEqualTo(10L);
        assertThat(matchProposal.getRequesterId()).isEqualTo(1L);
        assertThat(matchProposal.getProviderId()).isEqualTo(2L);
        assertThat(matchProposal.getRequestMessage()).isEqualTo("재능 교환 제안드립니다.");
        assertThat(matchProposal.getStatus()).isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(matchProposal.getRespondedAt()).isNull();
        assertThat(matchProposal.getProviderTalentPriceSnapshot()).isEqualTo(150);
        assertThat(matchProposal.getRequesterTalentPriceSnapshot()).isEqualTo(100);
        assertThat(matchProposal.getActiveSwapPairKey()).isEqualTo("10:20");
        assertThat(matchProposal.getTradeType()).isEqualTo(TradeType.SWAP);
        assertThat(matchProposal.isSwap()).isTrue();
    }

    @Test
    @DisplayName("단방향 구매 제안은 PURCHASE 타입이다.")
    void purchaseProposalTradeType() {
        MatchProposal matchProposal = MatchProposal.create(
                20L,
                null,
                1L,
                2L,
                "재능 구매 제안드립니다.",
                150,
                null,
                null
        );

        assertThat(matchProposal.getTradeType()).isEqualTo(TradeType.PURCHASE);
        assertThat(matchProposal.isSwap()).isFalse();
        assertThat(matchProposal.getRequesterTalentPriceSnapshot()).isNull();
        assertThat(matchProposal.getActiveSwapPairKey()).isNull();
    }

    @Test
    @DisplayName("매칭 제안을 수락하면 ACCEPTED 상태가 되고 응답 시간이 기록된다.")
    void acceptMatchProposal() {
        MatchProposal matchProposal = createSwapProposal();

        matchProposal.accept();

        assertThat(matchProposal.getStatus()).isEqualTo(MatchProposalStatus.ACCEPTED);
        assertThat(matchProposal.getRespondedAt()).isNotNull();
    }

    @Test
    @DisplayName("매칭 제안을 거절하면 REJECTED 상태가 되고 응답 시간이 기록되며 activeSwapPairKey가 해제된다.")
    void rejectMatchProposal() {
        MatchProposal matchProposal = createSwapProposal();

        matchProposal.reject();

        assertThat(matchProposal.getStatus()).isEqualTo(MatchProposalStatus.REJECTED);
        assertThat(matchProposal.getRespondedAt()).isNotNull();
        assertThat(matchProposal.getActiveSwapPairKey()).isNull();
    }

    @Test
    @DisplayName("activeSwapPairKey는 재능 ID 순서와 관계없이 같은 값이다.")
    void createActiveSwapPairKey() {
        String forwardKey = MatchProposal.createActiveSwapPairKey(10L, 20L);
        String reverseKey = MatchProposal.createActiveSwapPairKey(20L, 10L);

        assertThat(forwardKey).isEqualTo("10:20");
        assertThat(reverseKey).isEqualTo("10:20");
    }

    @Test
    @DisplayName("activeSwapPairKey 생성 시 재능 ID 중 하나가 null이면 null을 반환한다")
    void createActiveSwapPairKey_withNullTalentId() {
        assertThat(MatchProposal.createActiveSwapPairKey(null, 20L)).isNull();
        assertThat(MatchProposal.createActiveSwapPairKey(10L, null)).isNull();
    }

    private MatchProposal createSwapProposal() {
        return MatchProposal.create(
                20L,
                10L,
                1L,
                2L,
                "재능 교환 제안드립니다.",
                150,
                100,
                MatchProposal.createActiveSwapPairKey(10L, 20L)
        );
    }

    @Test
    @DisplayName("재능 정보로 교환 제안을 생성하면 가격 스냅샷과 activeSwapPairKey가 저장된다")
    void createFromTalents_swap() {
        Talent providerTalent = createTalent(20L, 2L, 150);
        Talent requesterTalent = createTalent(10L, 1L, 100);

        MatchProposal matchProposal = MatchProposal.createFromTalents(
                providerTalent,
                requesterTalent,
                1L,
                2L,
                "재능 교환 제안드립니다."
        );

        assertThat(matchProposal.getProviderTalentId()).isEqualTo(20L);
        assertThat(matchProposal.getRequesterTalentId()).isEqualTo(10L);
        assertThat(matchProposal.getProviderTalentPriceSnapshot()).isEqualTo(150);
        assertThat(matchProposal.getRequesterTalentPriceSnapshot()).isEqualTo(100);
        assertThat(matchProposal.getActiveSwapPairKey()).isEqualTo("10:20");
        assertThat(matchProposal.getTradeType()).isEqualTo(TradeType.SWAP);
        assertThat(matchProposal.isSwap()).isTrue();
    }

    @Test
    @DisplayName("재능 정보로 구매 제안을 생성하면 요청자 재능 정보와 activeSwapPairKey는 비어 있다")
    void createFromTalents_purchase() {
        Talent providerTalent = createTalent(20L, 2L, 150);

        MatchProposal matchProposal = MatchProposal.createFromTalents(
                providerTalent,
                null,
                1L,
                2L,
                "재능 구매 제안드립니다."
        );

        assertThat(matchProposal.getProviderTalentId()).isEqualTo(20L);
        assertThat(matchProposal.getRequesterTalentId()).isNull();
        assertThat(matchProposal.getProviderTalentPriceSnapshot()).isEqualTo(150);
        assertThat(matchProposal.getRequesterTalentPriceSnapshot()).isNull();
        assertThat(matchProposal.getActiveSwapPairKey()).isNull();
        assertThat(matchProposal.getTradeType()).isEqualTo(TradeType.PURCHASE);
        assertThat(matchProposal.isSwap()).isFalse();
    }

    private Talent createTalent(Long id, Long authorId, Integer creditPrice) {
        Talent talent = Talent.create(
                authorId,
                createCategory(),
                "테스트 재능",
                "테스트 내용",
                2,
                creditPrice
        );

        ReflectionTestUtils.setField(talent, "id", id);

        return talent;
    }

    private Category createCategory() {
        try {
            Constructor<Category> constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            Category category = constructor.newInstance();

            ReflectionTestUtils.setField(category, "id", 1L);
            ReflectionTestUtils.setField(category, "name", "백엔드");
            ReflectionTestUtils.setField(category, "sortOrder", 1);
            ReflectionTestUtils.setField(category, "active", true);

            return category;
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Category 생성 실패", e);
        }
    }
}