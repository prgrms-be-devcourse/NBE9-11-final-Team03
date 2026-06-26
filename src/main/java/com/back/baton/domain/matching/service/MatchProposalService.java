package com.back.baton.domain.matching.service;

import com.back.baton.domain.chat.dto.request.TradeChatRoomCreateReq;
import com.back.baton.domain.chat.service.ChatService;
import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.service.EscrowService;
import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalReceivedRes;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.dto.response.MatchProposalSentRes;
import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.service.TradeService;
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
    private final TradeService tradeService;
    private final CreditService creditService;
    private final EscrowService escrowService;
    private final ChatService chatService;

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
        validateDuplicatedProposal(requesterId, req); // 역방향 검증

        MatchProposal matchProposal = MatchProposal.createFromTalents(
                providerTalent,
                requesterTalent,
                requesterId,
                providerTalent.getAuthorId(),
                req.requestMessage()
        );

        MatchProposal savedMatchProposal = matchProposalRepository.save(matchProposal);

        return MatchProposalRes.from(savedMatchProposal);
    }

    @Transactional
    public MatchProposalRes acceptMatchProposal(
            Long proposalId,
            Long providerId
    ) {
        MatchProposal matchProposal = matchProposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomException(MatchingErrorCode.MATCH_PROPOSAL_NOT_FOUND));

        validateProviderAuthority(providerId, matchProposal);

        validateRequestedStatus(matchProposal);

        // TODO: Trade/Credit/Escrow의 SWAP 그룹 생성 구현 전까지 양방향 거래 수락 차단
        if (matchProposal.isSwap()) {
            throw new CustomException(MatchingErrorCode.SWAP_ACCEPT_NOT_IMPLEMENTED);
        }

        Talent providerTalent = getTalent(matchProposal.getProviderTalentId());
        validateProviderOwnsTalent(providerId, providerTalent);
        validateTalentAvailable(providerTalent);

        TradeType tradeType = matchProposal.getTradeType();


        // TODO: SWAP은 TradeGroup 1건, Trade 2건, Credit hold 2건, Escrow 2건 생성으로 연결
        // Trade/Credit/Escrow 구현 완료 후 이 분기에서 위 예외를 제거하고 연동
        // Trade trade = tradeService.create(matchProposal, price); 리팩토링 필요
        Trade trade = tradeService.create(
                matchProposal.getId(),
                matchProposal.getProviderTalentId(),
                matchProposal.getRequesterId(),
                matchProposal.getProviderId(),
                matchProposal.getProviderTalentPriceSnapshot(),
                tradeType
        );

        String escrowHoldIdempotencyKey = "MATCH-PROPOSAL-ACCEPT-"
                + matchProposal.getId()
                + ":TRADE-"
                + trade.getId();

        creditService.holdForEscrow(
                matchProposal.getRequesterId(),
                matchProposal.getProviderTalentPriceSnapshot(),
                trade.getId(),
                escrowHoldIdempotencyKey
        );

        escrowService.create(
                trade.getId(),
                matchProposal.getRequesterId(),
                matchProposal.getProviderId(),
                matchProposal.getProviderTalentPriceSnapshot()
        );

        matchProposal.accept();

        chatService.getOrCreateTransactionRoom(TradeChatRoomCreateReq.from(trade));

        MatchProposal savedMatchProposal = matchProposalRepository.save(matchProposal);

        return MatchProposalRes.from(savedMatchProposal);
    }

    @Transactional
    public MatchProposalRes rejectMatchProposal(Long proposalId, Long providerId) {
        MatchProposal matchProposal = matchProposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomException(MatchingErrorCode.MATCH_PROPOSAL_NOT_FOUND));

        validateRequestedStatus(matchProposal);
        validateProviderAuthority(providerId, matchProposal);

        matchProposal.reject();

        return MatchProposalRes.from(matchProposal);
    }

    public List<MatchProposalReceivedRes> getReceivedProposals(Long providerId, MatchProposalStatus status) {
        return matchProposalRepository.findReceivedProposals(providerId, status);
    }

    public List<MatchProposalSentRes> getSentProposals(Long requesterId, MatchProposalStatus status) {
        return matchProposalRepository.findSentProposals(requesterId, status);
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
        if (req.requesterTalentId() == null) {
            validateDuplicatedPurchaseProposal(requesterId, req);
            return;
        }

        validateDuplicatedSwapProposal(req);
    }

    private void validateDuplicatedPurchaseProposal(Long requesterId, MatchProposalCreateReq req) {
        boolean exists = matchProposalRepository.existsActiveProposal(
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

    private void validateDuplicatedSwapProposal(MatchProposalCreateReq req) {
        String activeSwapPairKey = MatchProposal.createActiveSwapPairKey(
                req.requesterTalentId(),
                req.providerTalentId()
        );

        if (matchProposalRepository.existsByActiveSwapPairKey(activeSwapPairKey)) {
            throw new CustomException(MatchingErrorCode.DUPLICATED_MATCHING_PROPOSAL);
        }
    }
}