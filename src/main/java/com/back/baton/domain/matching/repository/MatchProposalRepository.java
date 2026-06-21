package com.back.baton.domain.matching.repository;

import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.entity.MatchProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MatchProposalRepository extends JpaRepository<MatchProposal, Long> {

    @Query("""
    select count(mp) > 0
    from MatchProposal mp
    where mp.requesterId = :requesterId
      and mp.requesterTalentId = :requesterTalentId
      and mp.providerTalentId = :providerTalentId
      and mp.status in :statuses
    """)
    boolean existsActiveProposal(
            @Param("requesterId") Long requesterId,
            @Param("requesterTalentId") Long requesterTalentId,
            @Param("providerTalentId") Long providerTalentId,
            @Param("statuses") Collection<MatchProposalStatus> statuses
    );

    @Query("""
    select mp.providerTalentId
    from MatchProposal mp
    where mp.requesterId = :requesterId
      and mp.requesterTalentId = :requesterTalentId
      and mp.status in :statuses
    """)
    List<Long> findUnavailableProviderTalentIds(
            @Param("requesterId") Long requesterId,
            @Param("requesterTalentId") Long requesterTalentId,
            @Param("statuses") Collection<MatchProposalStatus> statuses
    );

    @Modifying(clearAutomatically = true)
    @Query("""
    update MatchProposal mp
    SET mp.status = :status
    WHERE mp.providerId = :providerId
    """)
    void updateStatusWhenProviderWithdrawn(
            @Param("providerId") Long providerId,
            @Param("status") MatchProposalStatus status
    );
}