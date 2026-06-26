package com.back.baton.domain.matching.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.category.repository.CategoryRepository;
import com.back.baton.domain.matching.dto.response.MatchProposalReceivedRes;
import com.back.baton.domain.matching.dto.response.MatchProposalSentRes;
import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.repository.UserRepository;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
class MatchProposalRepositoryTest {

    @Autowired
    private MatchProposalRepository matchProposalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("지정한 상태에 해당하는 providerTalentId만 조회한다")
    void findUnavailableProviderTalentIds() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        MatchProposal requestedProposal = createProposal(
                10L,
                requesterTalentId,
                requesterId,
                3L,
                "요청 중인 제안입니다."
        );

        MatchProposal acceptedProposal = createProposal(
                20L,
                requesterTalentId,
                requesterId,
                4L,
                "수락된 제안입니다."
        );
        acceptedProposal.accept();

        matchProposalRepository.save(requestedProposal);
        matchProposalRepository.save(acceptedProposal);

        List<Long> result = matchProposalRepository.findUnavailableProviderTalentIds(
                requesterId,
                requesterTalentId,
                List.of(MatchProposalStatus.REQUESTED)
        );

        assertThat(result).containsExactly(10L);
    }

    @Test
    @DisplayName("여러 상태에 해당하는 providerTalentId를 조회한다")
    void findUnavailableProviderTalentIds_withMultipleStatuses() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        MatchProposal requestedProposal = createProposal(
                10L,
                requesterTalentId,
                requesterId,
                3L,
                "요청 중인 제안입니다."
        );

        MatchProposal acceptedProposal = createProposal(
                20L,
                requesterTalentId,
                requesterId,
                4L,
                "수락된 제안입니다."
        );
        acceptedProposal.accept();

        matchProposalRepository.save(requestedProposal);
        matchProposalRepository.save(acceptedProposal);

        List<Long> result = matchProposalRepository.findUnavailableProviderTalentIds(
                requesterId,
                requesterTalentId,
                List.of(
                        MatchProposalStatus.REQUESTED,
                        MatchProposalStatus.ACCEPTED
                )
        );

        assertThat(result).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    @DisplayName("다른 요청자의 제안은 조회하지 않는다")
    void findUnavailableProviderTalentIds_excludeOtherRequester() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        MatchProposal myProposal = createProposal(
                10L,
                requesterTalentId,
                requesterId,
                3L,
                "내가 보낸 제안입니다."
        );

        MatchProposal otherRequesterProposal = createProposal(
                20L,
                requesterTalentId,
                999L,
                4L,
                "다른 사용자가 보낸 제안입니다."
        );

        matchProposalRepository.save(myProposal);
        matchProposalRepository.save(otherRequesterProposal);

        List<Long> result = matchProposalRepository.findUnavailableProviderTalentIds(
                requesterId,
                requesterTalentId,
                List.of(MatchProposalStatus.REQUESTED)
        );

        assertThat(result).containsExactly(10L);
    }

    @Test
    @DisplayName("다른 요청자 재능의 제안은 조회하지 않는다")
    void findUnavailableProviderTalentIds_excludeOtherRequesterTalent() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        MatchProposal myTalentProposal = createProposal(
                10L,
                requesterTalentId,
                requesterId,
                3L,
                "내 재능으로 보낸 제안입니다."
        );

        MatchProposal otherTalentProposal = createProposal(
                20L,
                999L,
                requesterId,
                4L,
                "다른 재능으로 보낸 제안입니다."
        );

        matchProposalRepository.save(myTalentProposal);
        matchProposalRepository.save(otherTalentProposal);

        List<Long> result = matchProposalRepository.findUnavailableProviderTalentIds(
                requesterId,
                requesterTalentId,
                List.of(MatchProposalStatus.REQUESTED)
        );

        assertThat(result).containsExactly(10L);
    }

    @Test
    @DisplayName("현재 로그인한 제공자가 받은 매칭 제안 목록을 조회한다")
    void findReceivedProposals() {
        TestFixture fixture = createProposalFixture();

        List<MatchProposalReceivedRes> result =
                matchProposalRepository.findReceivedProposals(
                        fixture.provider().getId(),
                        null
                );

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(MatchProposalReceivedRes::proposalId)
                .containsExactly(
                        fixture.otherRequesterProposal().getId(),
                        fixture.acceptedProposal().getId(),
                        fixture.requestedProposal().getId()
                );

        MatchProposalReceivedRes first = result.get(0);

        assertThat(first.status()).isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(first.requestMessage()).isEqualTo("다른 요청자가 보낸 제안입니다.");
        assertThat(first.requesterId()).isEqualTo(fixture.otherProvider().getId());
        assertThat(first.requesterNickname()).isEqualTo("다른제공자");
        assertThat(first.requesterProfileImageUrl()).isNull();
        assertThat(first.requesterTalentId()).isNull();
        assertThat(first.requesterTalentTitle()).isNull();
        assertThat(first.providerId()).isEqualTo(fixture.provider().getId());
        assertThat(first.providerTalentId()).isEqualTo(fixture.providerTalent().getId());
        assertThat(first.providerTalentTitle()).isEqualTo("React 화면 구현 도와드립니다");
    }

    @Test
    @DisplayName("받은 매칭 제안 목록은 상태로 필터링할 수 있다")
    void findReceivedProposals_withStatus() {
        TestFixture fixture = createProposalFixture();

        List<MatchProposalReceivedRes> result =
                matchProposalRepository.findReceivedProposals(
                        fixture.provider().getId(),
                        MatchProposalStatus.REQUESTED
                );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(MatchProposalReceivedRes::proposalId)
                .containsExactly(
                        fixture.otherRequesterProposal().getId(),
                        fixture.requestedProposal().getId()
                );

        MatchProposalReceivedRes first = result.get(0);

        assertThat(first.proposalId()).isEqualTo(fixture.otherRequesterProposal().getId());
        assertThat(first.status()).isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(first.requestMessage()).isEqualTo("다른 요청자가 보낸 제안입니다.");
        assertThat(first.requesterId()).isEqualTo(fixture.otherProvider().getId());
        assertThat(first.requesterTalentId()).isNull();
        assertThat(first.requesterTalentTitle()).isNull();

        MatchProposalReceivedRes second = result.get(1);

        assertThat(second.proposalId()).isEqualTo(fixture.requestedProposal().getId());
        assertThat(second.status()).isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(second.requestMessage()).isEqualTo("요청 중인 제안입니다.");
        assertThat(second.requesterId()).isEqualTo(fixture.requester().getId());
        assertThat(second.providerId()).isEqualTo(fixture.provider().getId());
    }

    @Test
    @DisplayName("현재 로그인한 요청자가 보낸 매칭 제안 목록을 조회한다")
    void findSentProposals() {
        TestFixture fixture = createProposalFixture();

        List<MatchProposalSentRes> result =
                matchProposalRepository.findSentProposals(
                        fixture.requester().getId(),
                        null
                );

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(MatchProposalSentRes::proposalId)
                .containsExactly(
                        fixture.otherProviderProposal().getId(),
                        fixture.acceptedProposal().getId(),
                        fixture.requestedProposal().getId()
                );

        MatchProposalSentRes first = result.get(0);

        assertThat(first.status()).isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(first.requestMessage()).isEqualTo("다른 제공자에게 보낸 제안입니다.");
        assertThat(first.requesterId()).isEqualTo(fixture.requester().getId());
        assertThat(first.requesterTalentId()).isEqualTo(fixture.requesterTalent().getId());
        assertThat(first.requesterTalentTitle()).isEqualTo("Spring Boot API 구현 도와드립니다");
        assertThat(first.providerId()).isEqualTo(fixture.otherProvider().getId());
        assertThat(first.providerNickname()).isEqualTo("다른제공자");
        assertThat(first.providerProfileImageUrl()).isNull();
        assertThat(first.providerTalentId()).isEqualTo(fixture.otherProviderTalent().getId());
        assertThat(first.providerTalentTitle()).isEqualTo("문서 정리 도와드립니다");
    }

    @Test
    @DisplayName("보낸 매칭 제안 목록은 상태로 필터링할 수 있다")
    void findSentProposals_withStatus() {
        TestFixture fixture = createProposalFixture();

        List<MatchProposalSentRes> result =
                matchProposalRepository.findSentProposals(
                        fixture.requester().getId(),
                        MatchProposalStatus.ACCEPTED
                );

        assertThat(result).hasSize(1);

        MatchProposalSentRes proposal = result.get(0);

        assertThat(proposal.proposalId()).isEqualTo(fixture.acceptedProposal().getId());
        assertThat(proposal.status()).isEqualTo(MatchProposalStatus.ACCEPTED);
        assertThat(proposal.requestMessage()).isEqualTo("수락된 제안입니다.");
        assertThat(proposal.requesterId()).isEqualTo(fixture.requester().getId());
        assertThat(proposal.providerId()).isEqualTo(fixture.provider().getId());
    }

    @Test
    @DisplayName("제공자 재능이 삭제된 제안은 받은 제안 목록에서 제외된다")
    void findReceivedProposals_excludeDeletedProviderTalent() {
        User requester = saveUser("requester@example.com", "요청자");
        User provider = saveUser("provider@example.com", "제공자");
        Category category = saveCategory("백엔드", 1);

        Talent requesterTalent = saveTalent(
                requester.getId(),
                category,
                "요청자 재능"
        );

        Talent activeProviderTalent = saveTalent(
                provider.getId(),
                category,
                "정상 제공자 재능"
        );

        Talent deletedProviderTalent = saveTalent(
                provider.getId(),
                category,
                "삭제된 제공자 재능"
        );
        deletedProviderTalent.softDelete();

        MatchProposal visibleProposal = matchProposalRepository.save(
                createProposal(
                        activeProviderTalent.getId(),
                        requesterTalent.getId(),
                        requester.getId(),
                        provider.getId(),
                        "정상 재능에 대한 제안입니다."
                )
        );

        matchProposalRepository.save(
                createProposal(
                        deletedProviderTalent.getId(),
                        requesterTalent.getId(),
                        requester.getId(),
                        provider.getId(),
                        "삭제된 재능에 대한 제안입니다."
                )
        );

        List<MatchProposalReceivedRes> result = matchProposalRepository.findReceivedProposals(
                provider.getId(),
                null
        );

        assertThat(result)
                .extracting(MatchProposalReceivedRes::proposalId)
                .containsExactly(visibleProposal.getId());
    }

    @Test
    @DisplayName("요청자 재능이 삭제된 교환 제안은 보낸 제안 목록에서 제외된다")
    void findSentProposals_excludeDeletedRequesterTalent() {
        User requester = saveUser("requester2@example.com", "요청자2");
        User provider = saveUser("provider2@example.com", "제공자2");
        Category category = saveCategory("프론트엔드", 2);

        Talent providerTalent = saveTalent(
                provider.getId(),
                category,
                "제공자 재능"
        );

        Talent activeRequesterTalent = saveTalent(
                requester.getId(),
                category,
                "정상 요청자 재능"
        );

        Talent deletedRequesterTalent = saveTalent(
                requester.getId(),
                category,
                "삭제된 요청자 재능"
        );
        deletedRequesterTalent.softDelete();

        MatchProposal visibleProposal = matchProposalRepository.save(
                createProposal(
                        providerTalent.getId(),
                        activeRequesterTalent.getId(),
                        requester.getId(),
                        provider.getId(),
                        "정상 교환 제안입니다."
                )
        );

        matchProposalRepository.save(
                createProposal(
                        providerTalent.getId(),
                        deletedRequesterTalent.getId(),
                        requester.getId(),
                        provider.getId(),
                        "삭제된 요청자 재능이 포함된 제안입니다."
                )
        );

        List<MatchProposalSentRes> result = matchProposalRepository.findSentProposals(
                requester.getId(),
                null
        );

        assertThat(result)
                .extracting(MatchProposalSentRes::proposalId)
                .containsExactly(visibleProposal.getId());
    }

    @Test
    @DisplayName("제공자 탈퇴로 제안 상태를 변경하면 activeSwapPairKey를 해제한다")
    void updateStatusWhenProviderWithdrawn_clearActiveSwapPairKey() {
        MatchProposal matchProposal = createProposal(
                20L,
                10L,
                1L,
                2L,
                "재능 교환 제안드립니다."
        );

        matchProposal = matchProposalRepository.saveAndFlush(matchProposal);

        matchProposalRepository.updateStatusWhenProviderWithdrawn(
                2L,
                MatchProposalStatus.REJECTED
        );

        entityManager.clear();

        MatchProposal found = matchProposalRepository.findById(matchProposal.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(MatchProposalStatus.REJECTED);
        assertThat(found.getActiveSwapPairKey()).isNull();
    }

    @Test
    @DisplayName("요청자 탈퇴로 제안 상태를 변경하면 activeSwapPairKey를 해제한다")
    void updateStatusWhenRequesterWithdrawn_clearActiveSwapPairKey() {
        MatchProposal matchProposal = createProposal(
                20L,
                10L,
                1L,
                2L,
                "재능 교환 제안드립니다."
        );

        matchProposal = matchProposalRepository.saveAndFlush(matchProposal);

        matchProposalRepository.updateStatusWhenRequesterWithdrawn(
                1L,
                MatchProposalStatus.CANCELLED
        );

        entityManager.clear();

        MatchProposal found = matchProposalRepository.findById(matchProposal.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(MatchProposalStatus.CANCELLED);
        assertThat(found.getActiveSwapPairKey()).isNull();
    }

    @Test
    @DisplayName("재능 삭제 시 해당 talentId의 REQUESTED 제안만 CANCELLED로 전이되고 updatedAt이 갱신되고 activeSwapPairKey를 해제한다")
    void cancelRequestedByTalentId_onlyRequestedOfTargetTalent() {
        User requester = saveUser("cancel-req@example.com", "취소요청자");
        User provider = saveUser("cancel-prov@example.com", "취소제공자");
        Category category = saveCategory("취소카테고리", 99);

        Talent targetTalent = saveTalent(provider.getId(), category, "삭제 대상 재능");
        Talent otherTalent = saveTalent(provider.getId(), category, "무관한 재능");
        Talent reqTalent = saveTalent(requester.getId(), category, "요청자 재능");
        Talent acceptedReqTalent = saveTalent(requester.getId(), category, "수락된 요청자 재능");

        Long targetId = targetTalent.getId();

        //target이 provider측, REQUESTED → CANCELLED
        MatchProposal p1 = matchProposalRepository.save(createProposal(
                targetId, reqTalent.getId(), requester.getId(), provider.getId(), "provider측 제안"));
        //target이 requester측, REQUESTED → CANCELLED
        MatchProposal p2 = matchProposalRepository.save(createProposal(
                otherTalent.getId(), targetId, requester.getId(), provider.getId(), "requester측 제안"));
        //target과 무관, REQUESTED → 유지
        MatchProposal p3 = matchProposalRepository.save(createProposal(
                otherTalent.getId(), reqTalent.getId(), requester.getId(), provider.getId(), "무관 제안"));
        //target이 provider측, ACCEPTED → 유지
        MatchProposal p4 = createProposal(
                targetId, acceptedReqTalent.getId(), requester.getId(), provider.getId(), "이미 수락됨");
        p4.accept();
        p4 = matchProposalRepository.save(p4);
        //target이 requester측, REJECTED → 유지
        MatchProposal p5 = createProposal(
                otherTalent.getId(), targetId, requester.getId(), provider.getId(), "이미 반려됨");
        p5.reject();
        p5 = matchProposalRepository.save(p5);

        LocalDateTime beforeUpdate = LocalDateTime.now();

        // when
        matchProposalRepository.cancelRequestedByTalentId(targetId);

        // then
        MatchProposal updated1 = matchProposalRepository.findById(p1.getId()).orElseThrow();
        MatchProposal updated2 = matchProposalRepository.findById(p2.getId()).orElseThrow();

        assertThat(updated1.getStatus()).isEqualTo(MatchProposalStatus.CANCELLED);
        assertThat(updated1.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        assertThat(updated1.getActiveSwapPairKey()).isNull();

        assertThat(updated2.getStatus()).isEqualTo(MatchProposalStatus.CANCELLED);
        assertThat(updated2.getActiveSwapPairKey()).isNull();

        assertThat(matchProposalRepository.findById(p3.getId()).orElseThrow().getStatus())
                .isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(matchProposalRepository.findById(p4.getId()).orElseThrow().getStatus())
                .isEqualTo(MatchProposalStatus.ACCEPTED);
        assertThat(matchProposalRepository.findById(p5.getId()).orElseThrow().getStatus())
                .isEqualTo(MatchProposalStatus.REJECTED);
    }

    private MatchProposal createProposal(
            Long providerTalentId,
            Long requesterTalentId,
            Long requesterId,
            Long providerId,
            String requestMessage
    ) {
        Integer requesterTalentPriceSnapshot = null;
        String activeSwapPairKey = null;

        if (requesterTalentId != null) {
            requesterTalentPriceSnapshot = 100;
            activeSwapPairKey = MatchProposal.createActiveSwapPairKey(
                    requesterTalentId,
                    providerTalentId
            );
        }

        return MatchProposal.create(
                providerTalentId,
                requesterTalentId,
                requesterId,
                providerId,
                requestMessage,
                100,
                requesterTalentPriceSnapshot,
                activeSwapPairKey
        );
    }

    private TestFixture createProposalFixture() {
        User requester = userRepository.save(createUser(
                "requester@test.com",
                "요청자"
        ));

        User provider = userRepository.save(createUser(
                "provider@test.com",
                "제공자"
        ));

        User otherProvider = userRepository.save(createUser(
                "other-provider@test.com",
                "다른제공자"
        ));

        Category category = categoryRepository.save(Category.create("개발", 1));

        Talent requesterTalent = talentRepository.save(Talent.create(
                requester.getId(),
                category,
                "Spring Boot API 구현 도와드립니다",
                "Spring Boot 기반 API 구현을 도와드립니다.",
                24,
                100
        ));

        Talent providerTalent = talentRepository.save(Talent.create(
                provider.getId(),
                category,
                "React 화면 구현 도와드립니다",
                "React 기반 화면 구현을 도와드립니다.",
                24,
                100
        ));

        Talent acceptedRequesterTalent = talentRepository.save(Talent.create(
                requester.getId(),
                category,
                "Spring Boot API 리팩토링 도와드립니다",
                "Spring Boot 기반 API 리팩토링을 도와드립니다.",
                24,
                100
        ));

        Talent acceptedProviderTalent = talentRepository.save(Talent.create(
                provider.getId(),
                category,
                "React 상태 관리 도와드립니다",
                "React 상태 관리 구현을 도와드립니다.",
                24,
                100
        ));

        Talent otherProviderTalent = talentRepository.save(Talent.create(
                otherProvider.getId(),
                category,
                "문서 정리 도와드립니다",
                "문서 정리와 README 작성을 도와드립니다.",
                24,
                100
        ));

        MatchProposal requestedProposal = matchProposalRepository.save(createProposal(
                providerTalent.getId(),
                requesterTalent.getId(),
                requester.getId(),
                provider.getId(),
                "요청 중인 제안입니다."
        ));

        MatchProposal acceptedProposal = createProposal(
                acceptedProviderTalent.getId(),
                acceptedRequesterTalent.getId(),
                requester.getId(),
                provider.getId(),
                "수락된 제안입니다."
        );
        acceptedProposal.accept();
        acceptedProposal = matchProposalRepository.save(acceptedProposal);

        MatchProposal otherProviderProposal = matchProposalRepository.save(createProposal(
                otherProviderTalent.getId(),
                requesterTalent.getId(),
                requester.getId(),
                otherProvider.getId(),
                "다른 제공자에게 보낸 제안입니다."
        ));

        MatchProposal otherRequesterProposal = matchProposalRepository.save(createProposal(
                providerTalent.getId(),
                null,
                otherProvider.getId(),
                provider.getId(),
                "다른 요청자가 보낸 제안입니다."
        ));

        return new TestFixture(
                requester,
                provider,
                otherProvider,
                requesterTalent,
                providerTalent,
                otherProviderTalent,
                requestedProposal,
                acceptedProposal,
                otherProviderProposal,
                otherRequesterProposal
        );
    }

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .password("password")
                .nickname(nickname)
                .profileImageUrl(null)
                .introduction("테스트 소개")
                .trustScore(BigDecimal.ZERO)
                .build();
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password("password")
                        .nickname(nickname)
                        .profileImageUrl(null)
                        .introduction("테스트 소개")
                        .trustScore(BigDecimal.ZERO)
                        .build()
        );
    }

    private Category saveCategory(String name, int sortOrder) {
        return categoryRepository.save(Category.create(name, sortOrder));
    }

    private Talent saveTalent(Long authorId, Category category, String title) {
        return talentRepository.save(
                Talent.create(
                        authorId,
                        category,
                        title,
                        "테스트 내용",
                        2,
                        100
                )
        );
    }

    private record TestFixture(
            User requester,
            User provider,
            User otherProvider,
            Talent requesterTalent,
            Talent providerTalent,
            Talent otherProviderTalent,
            MatchProposal requestedProposal,
            MatchProposal acceptedProposal,
            MatchProposal otherProviderProposal,
            MatchProposal otherRequesterProposal
    ) {
    }
}