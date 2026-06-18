package com.back.baton.domain.matching.repository;

import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.entity.MatchProposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchProposalRepository extends JpaRepository<MatchProposal, Long> {

    boolean existsByRequesterIdAndRequesterTalentIdAndProviderTalentIdAndStatus(
            Long requesterId,
            Long requesterTalentId,
            Long providerTalentId,
            MatchProposalStatus status
    );
}