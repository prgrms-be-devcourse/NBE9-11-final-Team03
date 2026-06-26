package com.back.baton.domain.matching.repository;

import com.back.baton.domain.matching.dto.response.MatchProposalReceivedRes;
import com.back.baton.domain.matching.dto.response.MatchProposalSentRes;
import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
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
    SET mp.status = :status,
        mp.activeSwapPairKey = null
    WHERE mp.providerId = :providerId AND mp.status != "ACCEPTED"
    """)
    void updateStatusWhenProviderWithdrawn(
            @Param("providerId") Long providerId,
            @Param("status") MatchProposalStatus status
    );

    @Modifying(clearAutomatically = true)
    @Query("""
    update MatchProposal mp
    SET mp.status = :status,
        mp.activeSwapPairKey = null
    WHERE mp.requesterId = :requesterId AND mp.status != "ACCEPTED"
    """)
    void updateStatusWhenRequesterWithdrawn(
            @Param("requesterId") Long requesterId,
            @Param("status") MatchProposalStatus status
    );

    default List<MatchProposalReceivedRes> findReceivedProposals(
            Long providerId,
            MatchProposalStatus status
    ) {
        if (status == null) {
            return findReceivedProposals(providerId);
        }

        return findReceivedProposalsByStatus(providerId, status);
    }

    @Query("""
    select new com.back.baton.domain.matching.dto.response.MatchProposalReceivedRes(
        mp.id,
        mp.status,
        mp.requestMessage,
        requester.id,
        requester.nickname,
        requester.profileImageUrl,
        requesterTalent.id,
        requesterTalent.title,
        mp.providerId,
        providerTalent.id,
        providerTalent.title,
        mp.createdAt
    )
    from MatchProposal mp
    join User requester on requester.id = mp.requesterId
    left join Talent requesterTalent on requesterTalent.id = mp.requesterTalentId
    join Talent providerTalent on providerTalent.id = mp.providerTalentId
    where mp.providerId = :providerId
      and providerTalent.deletedAt is null
      and (
            mp.requesterTalentId is null
            or (requesterTalent.id is not null and requesterTalent.deletedAt is null)
      )
    order by mp.createdAt desc
    """)
    List<MatchProposalReceivedRes> findReceivedProposals(
            @Param("providerId") Long providerId
    );

    @Query("""
    select new com.back.baton.domain.matching.dto.response.MatchProposalReceivedRes(
        mp.id,
        mp.status,
        mp.requestMessage,
        requester.id,
        requester.nickname,
        requester.profileImageUrl,
        requesterTalent.id,
        requesterTalent.title,
        mp.providerId,
        providerTalent.id,
        providerTalent.title,
        mp.createdAt
    )
    from MatchProposal mp
    join User requester on requester.id = mp.requesterId
    left join Talent requesterTalent on requesterTalent.id = mp.requesterTalentId
    join Talent providerTalent on providerTalent.id = mp.providerTalentId
    where mp.providerId = :providerId
      and mp.status = :status
      and providerTalent.deletedAt is null
      and (
            mp.requesterTalentId is null
            or (requesterTalent.id is not null and requesterTalent.deletedAt is null)
      )
    order by mp.createdAt desc
    """)
    List<MatchProposalReceivedRes> findReceivedProposalsByStatus(
            @Param("providerId") Long providerId,
            @Param("status") MatchProposalStatus status
    );

    default List<MatchProposalSentRes> findSentProposals(
            Long requesterId,
            MatchProposalStatus status
    ) {
        if (status == null) {
            return findSentProposals(requesterId);
        }

        return findSentProposalsByStatus(requesterId, status);
    }

    @Query("""
    select new com.back.baton.domain.matching.dto.response.MatchProposalSentRes(
        mp.id,
        mp.status,
        mp.requestMessage,
        mp.requesterId,
        requesterTalent.id,
        requesterTalent.title,
        provider.id,
        provider.nickname,
        provider.profileImageUrl,
        providerTalent.id,
        providerTalent.title,
        mp.createdAt
    )
    from MatchProposal mp
    join User provider on provider.id = mp.providerId
    left join Talent requesterTalent on requesterTalent.id = mp.requesterTalentId
    join Talent providerTalent on providerTalent.id = mp.providerTalentId
    where mp.requesterId = :requesterId
      and providerTalent.deletedAt is null
      and (
            mp.requesterTalentId is null
            or (requesterTalent.id is not null and requesterTalent.deletedAt is null)
      )
    order by mp.createdAt desc
    """)
    List<MatchProposalSentRes> findSentProposals(
            @Param("requesterId") Long requesterId
    );

    @Query("""
    select new com.back.baton.domain.matching.dto.response.MatchProposalSentRes(
        mp.id,
        mp.status,
        mp.requestMessage,
        mp.requesterId,
        requesterTalent.id,
        requesterTalent.title,
        provider.id,
        provider.nickname,
        provider.profileImageUrl,
        providerTalent.id,
        providerTalent.title,
        mp.createdAt
    )
    from MatchProposal mp
    join User provider on provider.id = mp.providerId
    left join Talent requesterTalent on requesterTalent.id = mp.requesterTalentId
    join Talent providerTalent on providerTalent.id = mp.providerTalentId
    where mp.requesterId = :requesterId
      and mp.status = :status
      and providerTalent.deletedAt is null
      and (
            mp.requesterTalentId is null
            or (requesterTalent.id is not null and requesterTalent.deletedAt is null)
      )
    order by mp.createdAt desc
    """)
    List<MatchProposalSentRes> findSentProposalsByStatus(
            @Param("requesterId") Long requesterId,
            @Param("status") MatchProposalStatus status
    );

    boolean existsByActiveSwapPairKey(String activeSwapPairKey);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update MatchProposal mp
            set mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.CANCELLED,
                mp.activeSwapPairKey = null,
                mp.updatedAt = CURRENT_TIMESTAMP
            where (mp.providerTalentId = :talentId or mp.requesterTalentId = :talentId)
            and mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.REQUESTED
            """)
    void cancelRequestedByTalentId(@Param("talentId") Long talentId);
}