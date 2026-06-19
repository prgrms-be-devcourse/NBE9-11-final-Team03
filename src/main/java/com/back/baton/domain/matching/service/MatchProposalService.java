package com.back.baton.domain.matching.service;

import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
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
public class MatchProposalService {

    private final MatchProposalRepository matchProposalRepository;
    private final TalentRepository talentRepository;

    @Transactional
    public MatchProposalRes createMatchProposal(Long requesterId, MatchProposalCreateReq req) {
        Talent providerTalent = getTalent(req.providerTalentId());

        Talent requesterTalent = null;
        if (req.requesterTalentId() != null) {
            requesterTalent = getTalent(req.requesterTalentId());
            validateRequesterOwnsTalent(requesterId, requesterTalent);
            validateTalentAvailable(requesterTalent);
        }

        validateProviderOwnsTalent(req.providerId(), providerTalent);
        validateTalentAvailable(providerTalent);
        validateSelfMatching(requesterId, req.providerId());
        validateDuplicatedProposal(requesterId, req);

        MatchProposal matchProposal = MatchProposal.create(
                providerTalent.getId(),
                requesterTalent == null ? null : requesterTalent.getId(),
                requesterId,
                providerTalent.getAuthorId(),
                req.requestMessage()
        );

        MatchProposal savedMatchProposal = matchProposalRepository.save(matchProposal);

        return MatchProposalRes.from(savedMatchProposal);
    }

    @Transactional
    public MatchProposalRes acceptMatchProposal(Long proposalId, Long providerId) {
        MatchProposal matchProposal = matchProposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomException(MatchingErrorCode.MATCH_PROPOSAL_NOT_FOUND));

        // MatchProposal 상태 및 수락 권한 검증
        validateRequestedStatus(matchProposal);
        validateProviderAuthority(providerId, matchProposal);

        // TODO: Credit/Trade/Escrow 기능 구현 완료 후 연동 필요
        //  - 구매자 크레딧 잔액 확인
        //  - 구매자 크레딧 에스크로 예치
        //  - 거래(Trade) 생성
        //  - 크레딧 거래 내역(CreditTransaction) 기록
        //  - 처리가 모두 성공한 뒤 매칭 제안을 ACCEPTED로 변경
        matchProposal.accept();

        return MatchProposalRes.from(matchProposal);
    }

    @Transactional
    public MatchProposalRes rejectMatchProposal(Long proposalId, Long providerId) {
        MatchProposal matchProposal = matchProposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomException(MatchingErrorCode.MATCH_PROPOSAL_NOT_FOUND));

        // MatchProposal 상태 및 거절 권한 검증
        validateRequestedStatus(matchProposal);
        validateProviderAuthority(providerId, matchProposal);

        matchProposal.reject();

        return MatchProposalRes.from(matchProposal);
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

    private void validateProviderOwnsTalent(Long providerId, Talent providerTalent) {
        if (!Objects.equals(providerTalent.getAuthorId(), providerId)) {
            throw new CustomException(MatchingErrorCode.MATCH_PROPOSAL_ACCESS_DENIED);
        }
    }

    private void validateTalentAvailable(Talent talent) {
        if (talent.getStatus() != TalentStatus.ACTIVE || talent.getDeletedAt() != null) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND);
        }
    }

    private void validateSelfMatching(Long requesterId, Long providerId) {
        if (Objects.equals(requesterId, providerId)) {
            throw new CustomException(MatchingErrorCode.SELF_MATCHING_NOT_ALLOWED);
        }
    }

    private void validateRequestedStatus(MatchProposal matchProposal) {
        if (matchProposal.getStatus() != MatchProposalStatus.REQUESTED) {
            throw new CustomException(MatchingErrorCode.INVALID_MATCHING_STATUS);
        }
    }

    private void validateProviderAuthority(Long providerId, MatchProposal matchProposal) {
        if (!Objects.equals(matchProposal.getProviderId(), providerId)) {
            throw new CustomException(MatchingErrorCode.MATCH_PROPOSAL_ACCESS_DENIED);
        }
    }

    private void validateDuplicatedProposal(Long requesterId, MatchProposalCreateReq req) {
        boolean exists = matchProposalRepository
                .existsActiveProposal(
                        requesterId,
                        req.requesterTalentId(),
                        req.providerTalentId(),
                        List.of(
                                MatchProposalStatus.REQUESTED,
                                MatchProposalStatus.ACCEPTED
                        )
                );

        if (exists) {
            throw new CustomException(MatchingErrorCode.DUPLICATED_MATCHING_PROPOSAL);
        }
    }
}
