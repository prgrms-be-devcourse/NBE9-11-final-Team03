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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchRecommendationService {

    private final TalentRepository talentRepository;
    private final MatchProposalRepository matchProposalRepository;
    private final MatchRecommendationQueryRepository matchRecommendationQueryRepository;

    public List<MatchRecommendationRes> getMatchRecommendations(Long talentId, Long userId) {
        Talent requesterTalent = getTalent(talentId);

        validateTalentAvailable(requesterTalent);
        validateRequesterOwnsTalent(userId, requesterTalent);

        List<Long> excludedTalentIds = matchProposalRepository.findRequestedProviderTalentIds(
                userId,
                requesterTalent.getId(),
                MatchProposalStatus.REQUESTED
        );

        return matchRecommendationQueryRepository.findMatchRecommendations(
                requesterTalent.getCategory().getId(),
                userId,
                excludedTalentIds
        );
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