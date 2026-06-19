package com.back.baton.domain.matching.service;

import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.matching.repository.MatchRecommendationQueryRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.MatchingErrorCode;
import com.back.baton.global.response.code.TalentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchRecommendationService {

    private static final String PROPOSAL_REQUEST_DISABLED_REASON = "이미 진행 중인 제안이 있습니다.";

    private final TalentRepository talentRepository;
    private final MatchRecommendationQueryRepository matchRecommendationQueryRepository;
    private final MatchProposalRepository matchProposalRepository;

    public List<MatchRecommendationRes> getMatchRecommendations(Long talentId, Long userId) {
        Talent requesterTalent = getTalent(talentId);

        validateTalentAvailable(requesterTalent);
        validateRequesterOwnsTalent(userId, requesterTalent);

        List<MatchRecommendationRes> recommendations =
                matchRecommendationQueryRepository.findMatchRecommendations(
                        requesterTalent.getCategory().getId(),
                        userId
                );

        Set<Long> unavailableProviderTalentIds = Set.copyOf(
                matchProposalRepository.findUnavailableProviderTalentIds(
                        userId,
                        requesterTalent.getId(),
                        List.of(
                                MatchProposalStatus.REQUESTED,
                                MatchProposalStatus.ACCEPTED
                        )
                )
        );

        return recommendations.stream()
                .map(recommendation -> {
                    if (unavailableProviderTalentIds.contains(recommendation.talentId())) {
                        return new MatchRecommendationRes(
                                recommendation.talentId(),
                                recommendation.providerId(),
                                recommendation.categoryId(),
                                recommendation.categoryName(),
                                recommendation.title(),
                                recommendation.content(),
                                recommendation.creditPrice(),
                                recommendation.estimatedHours(),
                                recommendation.avgRating(),
                                recommendation.completeCount(),
                                false,
                                PROPOSAL_REQUEST_DISABLED_REASON
                        );
                    }

                    return recommendation;
                })
                .toList();
    }

    private Talent getTalent(Long talentId) {
        return talentRepository.findById(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));
    }

    private void validateRequesterOwnsTalent(Long requesterId, Talent requesterTalent) {
        if (!Objects.equals(requesterTalent.getAuthorId(), requesterId)) {
            throw new CustomException(MatchingErrorCode.MATCH_PROPOSAL_ACCESS_DENIED);
        }
    }

    private void validateTalentAvailable(Talent talent) {
        if (talent.getStatus() != TalentStatus.ACTIVE || talent.isDeleted()) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND);
        }
    }
}