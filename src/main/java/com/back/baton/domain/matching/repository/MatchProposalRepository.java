package com.back.baton.domain.matching.repository;

import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.entity.MatchProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchProposalRepository extends JpaRepository<MatchProposal, Long> {

    boolean existsByRequesterIdAndRequesterTalentIdAndProviderTalentIdAndStatus(
            Long requesterId,
            Long requesterTalentId,
            Long providerTalentId,
            MatchProposalStatus status
    );

    @Query("""
        select mp.providerTalentId
        from MatchProposal mp
        where mp.requesterId = :requesterId
          and mp.requesterTalentId = :requesterTalentId
          and mp.status = :status
        """)
    List<Long> findRequestedProviderTalentIds(
            @Param("requesterId") Long requesterId,
            @Param("requesterTalentId") Long requesterTalentId,
            @Param("status") MatchProposalStatus status
    );
}