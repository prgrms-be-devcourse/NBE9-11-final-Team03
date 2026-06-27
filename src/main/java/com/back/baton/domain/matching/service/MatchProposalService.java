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
import com.back.baton.domain.trade.entity.TradeGroup;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.service.TradeGroupService;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.MatchingErrorCode;
import com.back.baton.global.response.code.TalentErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final TradeGroupService tradeGroupService;
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
        validateDuplicatedProposal(requesterId, req); // 정방향/역방향 검증

        MatchProposal matchProposal = MatchProposal.createFromTalents(
                providerTalent,
                requesterTalent,
                requesterId,
                providerTalent.getAuthorId(),
                req.requestMessage()
        );

        try {
            MatchProposal savedMatchProposal =
                    matchProposalRepository.saveAndFlush(matchProposal);

            return MatchProposalRes.from(savedMatchProposal);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(MatchingErrorCode.DUPLICATED_MATCHING_PROPOSAL);
        }
    }

    @Transactional
    public MatchProposalRes acceptMatchProposal(
            Long proposalId,
            Long providerId
    ) {
        MatchProposal matchProposal = matchProposalRepository.findByIdWithLock(proposalId)
                .orElseThrow(() -> new CustomException(MatchingErrorCode.MATCH_PROPOSAL_NOT_FOUND));

        validateProviderAuthority(providerId, matchProposal);

        validateRequestedStatus(matchProposal);

        TradeType tradeType = matchProposal.getTradeType();

        if (tradeType == TradeType.PURCHASE) {
            acceptPurchaseProposal(matchProposal, providerId);
        } else {
            acceptSwapProposal(matchProposal, providerId);
        }

        matchProposal.accept();

        MatchProposal savedMatchProposal = matchProposalRepository.save(matchProposal);

        return MatchProposalRes.from(savedMatchProposal);
    }

    private void acceptPurchaseProposal(MatchProposal matchProposal, Long providerId) {
        validateProviderTalentForAccept(providerId, matchProposal);

        Trade trade = tradeService.createPurchaseTrade(matchProposal);

        creditService.holdForEscrow(
                matchProposal.getRequesterId(),
                matchProposal.getProviderTalentPriceSnapshot(),
                trade.getId()
        );

        escrowService.create(
                trade.getId(),
                matchProposal.getRequesterId(),
                matchProposal.getProviderId(),
                matchProposal.getProviderTalentPriceSnapshot()
        );

        chatService.getOrCreateTransactionRoom(TradeChatRoomCreateReq.from(trade));
    }

    private void acceptSwapProposal(MatchProposal matchProposal, Long providerId) {
        // provider/requester talent 재검증
        validateProviderTalentForAccept(providerId, matchProposal);
        validateRequesterTalentForAccept(matchProposal);

        // TradeGroup 생성
        TradeGroup tradeGroup = tradeGroupService.create(
                matchProposal.getId(),
                TradeType.SWAP
        );

        // Trade 2건 생성
        List<Trade> trades = tradeService.createSwapTrades(matchProposal, tradeGroup);

        Trade requesterReceivesTrade = findSwapTrade(
                trades,
                matchProposal.getProviderTalentId(),
                matchProposal.getRequesterId(),
                matchProposal.getProviderId()
        );

        Trade providerReceivesTrade = findSwapTrade(
                trades,
                matchProposal.getRequesterTalentId(),
                matchProposal.getProviderId(),
                matchProposal.getRequesterId()
        );

        List<SwapEscrowLeg> escrowLegs = List.of(
                new SwapEscrowLeg(
                        requesterReceivesTrade,
                        matchProposal.getRequesterId(), // payer
                        matchProposal.getProviderId(),  // payee
                        matchProposal.getProviderTalentPriceSnapshot()
                ),
                new SwapEscrowLeg(
                        providerReceivesTrade,
                        matchProposal.getProviderId(),  // payer
                        matchProposal.getRequesterId(), // payee
                        matchProposal.getRequesterTalentPriceSnapshot()
                )
        );

        // Credit hold 2건
        // Escrow 2건
        for (SwapEscrowLeg escrowLeg : escrowLegs) {
            creditService.holdForEscrow(
                    escrowLeg.payerId(),
                    escrowLeg.amount(),
                    escrowLeg.trade().getId()
            );

            escrowService.create(
                    escrowLeg.trade().getId(),
                    escrowLeg.payerId(),
                    escrowLeg.payeeId(),
                    escrowLeg.amount()
            );
        }

        // TradeGroup 기준 채팅방 1개 생성
        chatService.getOrCreateSwapTransactionRoom(
                tradeGroup.getId(),
                matchProposal.getProviderTalentId(),
                matchProposal.getRequesterId(),
                matchProposal.getProviderId()
        );
    }

    @Transactional
    public MatchProposalRes rejectMatchProposal(Long proposalId, Long providerId) {
        MatchProposal matchProposal = matchProposalRepository.findByIdWithLock(proposalId)
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

    private Trade findSwapTrade(
            List<Trade> trades,
            Long talentId,
            Long buyerId,
            Long sellerId
    ) {
        return trades.stream()
                .filter(trade -> Objects.equals(trade.getTalentId(), talentId))
                .filter(trade -> Objects.equals(trade.getBuyerId(), buyerId))
                .filter(trade -> Objects.equals(trade.getSellerId(), sellerId))
                .findFirst()
                .orElseThrow(() -> new CustomException(TradeErrorCode.INVALID_SWAP_TRADE_MAPPING));
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

    private void validateProviderTalentForAccept(Long providerId, MatchProposal matchProposal) {
        Talent providerTalent = getTalent(matchProposal.getProviderTalentId());
        validateProviderOwnsTalent(providerId, providerTalent);
        validateTalentAvailable(providerTalent);
    }

    private void validateRequesterTalentForAccept(MatchProposal matchProposal) {
        Long requesterTalentId = matchProposal.getRequesterTalentId();

        if (requesterTalentId == null) {
            throw new CustomException(TalentErrorCode.TALENT_NOT_FOUND);
        }

        Talent requesterTalent = getTalent(requesterTalentId);
        validateRequesterOwnsTalent(matchProposal.getRequesterId(), requesterTalent);
        validateTalentAvailable(requesterTalent);
    }

    private record SwapEscrowLeg(
            Trade trade,
            Long payerId,
            Long payeeId,
            Integer amount
    ) {
    }
}
