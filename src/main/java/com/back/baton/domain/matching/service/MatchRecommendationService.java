package com.back.baton.domain.matching.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.matching.dto.response.MatchRecommendationDetailRes;
import com.back.baton.domain.matching.dto.response.MatchRecommendationRes;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.matching.repository.MatchRecommendationQueryRepository;
import com.back.baton.domain.profile.repository.ProfileRepository;
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

    private static final String SENT_PENDING_PROPOSAL_REASON = "이미 보낸 교환 제안이 대기 중입니다.";
    private static final String RECEIVED_PENDING_PROPOSAL_REASON = "상대가 보낸 교환 제안이 있습니다. 받은 제안을 확인해 주세요.";
    private static final String TRADE_IN_PROGRESS_REASON = "이미 진행 중인 교환 거래가 있습니다.";

    private final TalentRepository talentRepository;
    private final ProfileRepository profileRepository;
    private final MatchRecommendationQueryRepository matchRecommendationQueryRepository;
    private final MatchProposalRepository matchProposalRepository;

    public List<MatchRecommendationRes> getMatchRecommendations(Long userId) {
        List<Long> requesterWantCategoryIds = getRequesterWantCategoryIds(userId);

        List<MatchRecommendationRes> recommendations =
                matchRecommendationQueryRepository.findMatchRecommendations(
                        requesterWantCategoryIds,
                        userId
                );

        return recommendations.stream()
                .map(recommendation -> {
                    String disabledReason = resolveProposalDisabledReason(
                            userId,
                            recommendation.requesterTalentId(),
                            recommendation.providerId(),
                            recommendation.talentId()
                    );

                    if (disabledReason == null) {
                        return recommendation;
                    }

                    return new MatchRecommendationRes(
                            recommendation.talentId(),
                            recommendation.requesterTalentId(),
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
                            disabledReason
                    );
                })
                .toList();
    }

    public MatchRecommendationDetailRes getMatchRecommendationDetail(
            Long requesterTalentId,
            Long providerTalentId,
            Long userId
    ) {
        Talent requesterTalent = getTalent(requesterTalentId);

        validateTalentAvailable(requesterTalent);
        validateRequesterOwnsTalent(userId, requesterTalent);

        List<Long> requesterWantCategoryIds = getRequesterWantCategoryIds(userId);

        MatchRecommendationDetailRes detail =
                matchRecommendationQueryRepository.findMatchRecommendationDetail(
                                requesterTalent.getCategory().getId(),
                                requesterWantCategoryIds,
                                providerTalentId
                        )
                        .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));

        if (Objects.equals(detail.providerId(), userId)) {
            throw new CustomException(MatchingErrorCode.SELF_MATCHING_NOT_ALLOWED);
        }

        String disabledReason = resolveProposalDisabledReason(
                userId,
                requesterTalentId,
                detail.providerId(),
                providerTalentId
        );

        if (disabledReason == null) {
            return detail;
        }

        return detail.withProposalDisabled(disabledReason);
    }

    private Talent getTalent(Long talentId) {
        return talentRepository.findById(talentId)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));
    }

    private List<Long> getRequesterWantCategoryIds(Long userId) {
        List<Long> wantCategoryIds = profileRepository.findWantTalentCategoriesByUserId(userId).stream()
                .map(Category::getId)
                .toList();

        if(wantCategoryIds.isEmpty()) {
            throw new CustomException(MatchingErrorCode.WANT_TALENT_CATEGORY_REQUIRED);
        }

        return wantCategoryIds;
    }

    private String resolveProposalDisabledReason(
            Long requesterId,
            Long requesterTalentId,
            Long providerId,
            Long providerTalentId
    ) {
        if (matchProposalRepository.existsSentPendingProposal(
                requesterId,
                requesterTalentId,
                providerTalentId
        )) {
            return SENT_PENDING_PROPOSAL_REASON;
        }

        if (matchProposalRepository.existsReceivedPendingProposal(
                requesterId,
                requesterTalentId,
                providerId,
                providerTalentId
        )) {
            return RECEIVED_PENDING_PROPOSAL_REASON;
        }

        if (matchProposalRepository.existsTradeInProgressProposal(
                requesterId,
                requesterTalentId,
                providerTalentId
        )) {
            return TRADE_IN_PROGRESS_REASON;
        }

        return null;
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
