package com.back.baton.domain.matching.repository;

import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.back.baton.global.config.QueryDslConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
class MatchProposalRepositoryTest {

    @Autowired
    private MatchProposalRepository matchProposalRepository;

    @Test
    @DisplayName("REQUESTED 상태의 providerTalentId만 조회한다")
    void findRequestedProviderTalentIds() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        MatchProposal requestedProposal = MatchProposal.create(
                10L,
                requesterTalentId,
                requesterId,
                3L,
                "요청 중인 제안입니다."
        );

        MatchProposal acceptedProposal = MatchProposal.create(
                20L,
                requesterTalentId,
                requesterId,
                4L,
                "수락된 제안입니다."
        );
        acceptedProposal.accept();

        matchProposalRepository.save(requestedProposal);
        matchProposalRepository.save(acceptedProposal);

        List<Long> result = matchProposalRepository.findRequestedProviderTalentIds(
                requesterId,
                requesterTalentId,
                MatchProposalStatus.REQUESTED
        );

        assertThat(result).containsExactly(10L);
    }

    @Test
    @DisplayName("다른 요청자의 제안은 조회하지 않는다")
    void findRequestedProviderTalentIds_excludeOtherRequester() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        MatchProposal myProposal = MatchProposal.create(
                10L,
                requesterTalentId,
                requesterId,
                3L,
                "내가 보낸 제안입니다."
        );

        MatchProposal otherRequesterProposal = MatchProposal.create(
                20L,
                requesterTalentId,
                999L,
                4L,
                "다른 사용자가 보낸 제안입니다."
        );

        matchProposalRepository.save(myProposal);
        matchProposalRepository.save(otherRequesterProposal);

        List<Long> result = matchProposalRepository.findRequestedProviderTalentIds(
                requesterId,
                requesterTalentId,
                MatchProposalStatus.REQUESTED
        );

        assertThat(result).containsExactly(10L);
    }

    @Test
    @DisplayName("다른 요청자 재능의 제안은 조회하지 않는다")
    void findRequestedProviderTalentIds_excludeOtherRequesterTalent() {
        Long requesterId = 2L;
        Long requesterTalentId = 1L;

        MatchProposal myTalentProposal = MatchProposal.create(
                10L,
                requesterTalentId,
                requesterId,
                3L,
                "내 재능으로 보낸 제안입니다."
        );

        MatchProposal otherTalentProposal = MatchProposal.create(
                20L,
                999L,
                requesterId,
                4L,
                "다른 재능으로 보낸 제안입니다."
        );

        matchProposalRepository.save(myTalentProposal);
        matchProposalRepository.save(otherTalentProposal);

        List<Long> result = matchProposalRepository.findRequestedProviderTalentIds(
                requesterId,
                requesterTalentId,
                MatchProposalStatus.REQUESTED
        );

        assertThat(result).containsExactly(10L);
    }
}