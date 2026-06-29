package com.back.baton.domain.matching.repository;

import com.back.baton.domain.matching.dto.response.MatchProposalReceivedRes;
import com.back.baton.domain.matching.dto.response.MatchProposalSentRes;
import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchProposalRepository extends JpaRepository<MatchProposal, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mp from MatchProposal mp where mp.id = :id")
    Optional<MatchProposal> findByIdWithLock(@Param("id") Long id);

    @Query("""
            select count(mp) > 0
            from MatchProposal mp
            where mp.requesterId = :requesterId
              and (
                    (:requesterTalentId is null and mp.requesterTalentId is null)
                    or mp.requesterTalentId = :requesterTalentId
              )
              and mp.providerTalentId = :providerTalentId
              and (
                  mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.REQUESTED
                  or (
                      mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.ACCEPTED
                      and exists (
                          select t.id
                          from Trade t
                          where t.matchId = mp.id
                            and t.status not in (
                                com.back.baton.domain.trade.entity.TradeStatus.COMPLETED,
                                com.back.baton.domain.trade.entity.TradeStatus.CANCELLED
                            )
                      )
                  )
              )
            """)
    boolean existsActiveProposal(
            @Param("requesterId") Long requesterId,
            @Param("requesterTalentId") Long requesterTalentId,
            @Param("providerTalentId") Long providerTalentId
    );

    @Query("""
            select count(mp) > 0
            from MatchProposal mp
            where mp.requesterId = :requesterId
              and mp.requesterTalentId = :requesterTalentId
              and mp.providerTalentId = :providerTalentId
              and mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.REQUESTED
            """)
    boolean existsSentPendingProposal(
            @Param("requesterId") Long requesterId,
            @Param("requesterTalentId") Long requesterTalentId,
            @Param("providerTalentId") Long providerTalentId
    );

    @Query("""
            select count(mp) > 0
            from MatchProposal mp
            where mp.requesterId = :providerId
              and mp.requesterTalentId = :providerTalentId
              and mp.providerId = :requesterId
              and mp.providerTalentId = :requesterTalentId
              and mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.REQUESTED
            """)
    boolean existsReceivedPendingProposal(
            @Param("requesterId") Long requesterId,
            @Param("requesterTalentId") Long requesterTalentId,
            @Param("providerId") Long providerId,
            @Param("providerTalentId") Long providerTalentId
    );

    @Query("""
            select count(mp) > 0
            from MatchProposal mp
            where mp.requesterId = :requesterId
              and mp.requesterTalentId = :requesterTalentId
              and mp.providerTalentId = :providerTalentId
              and mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.ACCEPTED
              and exists (
                  select t.id
                  from Trade t
                  where t.matchId = mp.id
                    and t.status not in (
                        com.back.baton.domain.trade.entity.TradeStatus.COMPLETED,
                        com.back.baton.domain.trade.entity.TradeStatus.CANCELLED
                    )
              )
            """)
    boolean existsTradeInProgressProposal(
            @Param("requesterId") Long requesterId,
            @Param("requesterTalentId") Long requesterTalentId,
            @Param("providerTalentId") Long providerTalentId
    );

    @Query("""
            select mp.providerTalentId
            from MatchProposal mp
            where mp.requesterId = :requesterId
              and mp.requesterTalentId = :requesterTalentId
              and (
                  mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.REQUESTED
                  or (
                      mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.ACCEPTED
                      and exists (
                          select t.id
                          from Trade t
                          where t.matchId = mp.id
                            and t.status not in (
                                com.back.baton.domain.trade.entity.TradeStatus.COMPLETED,
                                com.back.baton.domain.trade.entity.TradeStatus.CANCELLED
                            )
                      )
                  )
              )
            """)
    List<Long> findUnavailableProviderTalentIds(
            @Param("requesterId") Long requesterId,
            @Param("requesterTalentId") Long requesterTalentId
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            update MatchProposal mp
            SET mp.status = :status,
                mp.activeSwapPairKey = null
            WHERE mp.providerId = :providerId AND mp.status = :pendingStatus
            """)
    void updateStatusWhenProviderWithdrawn(
            @Param("providerId") Long providerId,
            @Param("status") MatchProposalStatus status,
            @Param("pendingStatus") MatchProposalStatus pendingStatus
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            update MatchProposal mp
            SET mp.status = :status,
                mp.activeSwapPairKey = null
            WHERE mp.requesterId = :requesterId AND mp.status = :pendingStatus
            """)
    void updateStatusWhenRequesterWithdrawn(
            @Param("requesterId") Long requesterId,
            @Param("status") MatchProposalStatus status,
            @Param("pendingStatus") MatchProposalStatus pendingStatus
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

    @Query("""
            select count(mp) > 0
            from MatchProposal mp
            where mp.activeSwapPairKey = :activeSwapPairKey
              and (
                  mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.REQUESTED
                  or (
                      mp.status = com.back.baton.domain.matching.entity.MatchProposalStatus.ACCEPTED
                      and exists (
                          select t.id
                          from Trade t
                          where t.matchId = mp.id
                            and t.status not in (
                                com.back.baton.domain.trade.entity.TradeStatus.COMPLETED,
                                com.back.baton.domain.trade.entity.TradeStatus.CANCELLED
                            )
                      )
                  )
              )
            """)
    boolean existsByActiveSwapPairKey(@Param("activeSwapPairKey") String activeSwapPairKey);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update MatchProposal mp
            set mp.activeSwapPairKey = null,
                mp.updatedAt = CURRENT_TIMESTAMP
            where mp.id = :matchProposalId
            """)
    void clearActiveSwapPairKeyById(@Param("matchProposalId") Long matchProposalId);

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