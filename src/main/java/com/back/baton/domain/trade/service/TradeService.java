package com.back.baton.domain.trade.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.matching.repository.MatchProposalRepository;
import com.back.baton.domain.trade.dto.response.DisputeRes;
import com.back.baton.domain.trade.dto.response.TradeListRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.entity.DisputeVerdict;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.matching.entity.MatchProposal;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.back.baton.domain.trade.entity.TradeGroup;
import com.back.baton.domain.trade.repository.TradeGroupRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final EscrowRepository escrowRepository;
    private final CreditService creditService;
    private final MatchProposalRepository matchProposalRepository;

    // 단방향 거래 생성
    @Transactional
    public Trade createPurchaseTrade(MatchProposal matchProposal) {
        Trade trade = Trade.create(
                matchProposal.getId(),
                null, // 단방향 null 처리
                matchProposal.getProviderTalentId(),
                matchProposal.getRequesterId(),
                matchProposal.getProviderId(),
                matchProposal.getProviderTalentPriceSnapshot(),
                TradeType.PURCHASE
        );
        return tradeRepository.save(trade);
    }

    // 양방향 거래 생성
    @Transactional
    public List<Trade> createSwapTrades(MatchProposal matchProposal, TradeGroup tradeGroup) {
        Trade requesterTrade = Trade.create(
                matchProposal.getId(),
                tradeGroup.getId(),
                matchProposal.getProviderTalentId(),
                matchProposal.getRequesterId(),
                matchProposal.getProviderId(),
                matchProposal.getProviderTalentPriceSnapshot(),
                TradeType.SWAP
        );

        Trade providerTrade = Trade.create(
                matchProposal.getId(),
                tradeGroup.getId(),
                matchProposal.getRequesterTalentId(),
                matchProposal.getProviderId(),
                matchProposal.getRequesterId(),
                matchProposal.getRequesterTalentPriceSnapshot(),
                TradeType.SWAP
        );
        return tradeRepository.saveAll(List.of(requesterTrade, providerTrade));
    }

    public TradeRes getMyTrade(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        validateTradeParticipant(trade, userId);

        Escrow escrow = escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

        return TradeRes.of(trade, escrow);
    }

    @Transactional
    public TradeRes cancelTrade(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findByIdWithLock(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        validateTradeParticipant(trade, userId);
        validateCancellable(trade);

        // 양방향
        if (trade.getTradeType() == TradeType.SWAP) {
            List<Trade> trades = tradeRepository.findAllByTradeGroupId(trade.getTradeGroupId());
            for (Trade t : trades) {
                t.cancel(); // 거래 취소로 상태 변경

                Escrow escrow = escrowRepository.findByTradeId(t.getId())
                        .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
                escrow.refund(); // 에스크로 환불 상태로 변경

                creditService.refundFromEscrow(
                        escrow.getPayerId(),
                        escrow.getAmount(),
                        t.getId()
                );
            }

            matchProposalRepository.clearActiveSwapPairKeyById(trade.getMatchId());

            // 반환용 Response는 현재 요청받은 거래 기준
            Escrow requesterEscrow = escrowRepository.findByTradeId(tradeId)
                    .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
            return TradeRes.of(trade, requesterEscrow);

            // 단방향
        } else {
            Escrow escrow = escrowRepository.findByTradeId(tradeId)
                    .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

            trade.cancel();
            escrow.refund();

            creditService.refundFromEscrow(
                    escrow.getPayerId(),
                    escrow.getAmount(),
                    tradeId
            );

            return TradeRes.of(trade, escrow);
        }
    }

    // 실제 사용자용 구매 확정
    @Transactional
    public TradeRes confirmPurchase(Long tradeId, Long buyerId) {
        Trade trade = tradeRepository.findByIdWithLock(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));
        validateBuyer(trade, buyerId);
        validateUnderReview(trade);
        Escrow escrow = escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
        return processPurchaseConfirmation(trade, escrow);
    }

    // 스케줄러용 자동 구매 확정
    @Transactional
    public TradeRes autoConfirm(Long tradeId) {
        Trade trade = tradeRepository.findByIdWithLock(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));
        if (trade.getStatus() != TradeStatus.UNDER_REVIEW) {
            throw new CustomException(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);
        }
        Escrow escrow = escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
        return processPurchaseConfirmation(trade, escrow);
    }

    // 공통 구매 확정 로직
    private TradeRes processPurchaseConfirmation(Trade trade, Escrow escrow) {
        // 양방향
        if (trade.getTradeType() == TradeType.SWAP) {
            List<Trade> trades = tradeRepository.findAllByTradeGroupId(trade.getTradeGroupId());
            Trade partnerTrade = trades.stream()
                    .filter(t -> !t.getId().equals(trade.getId()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));
            if (partnerTrade.getStatus() == TradeStatus.AWAITING_PARTNER) {
                trade.complete();
                partnerTrade.complete();
                for (Trade t : trades) {
                    Escrow e = escrowRepository.findByTradeId(t.getId())
                            .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
                    e.release();
                    creditService.settleEscrow(
                            e.getPayerId(),
                            e.getPayeeId(),
                            e.getAmount(),
                            e.getAmount(),
                            t.getId()
                    );
                }

                matchProposalRepository.clearActiveSwapPairKeyById(trade.getMatchId());
            }
            else if (partnerTrade.getStatus() == TradeStatus.UNDER_REVIEW) {
                trade.waitPartner();
            }
            else {
                throw new CustomException(TradeErrorCode.PARTNER_TRADE_NOT_READY);
            }
            return TradeRes.of(trade, escrow);
        }
        // 단방향
        else {
            trade.complete();
            escrow.release();
            creditService.settleEscrow(
                    escrow.getPayerId(),
                    escrow.getPayeeId(),
                    escrow.getAmount(),
                    escrow.getAmount(),
                    trade.getId()
            );
            return TradeRes.of(trade, escrow);
        }
    }

    @Transactional
    public TradeRes disputeTrade(Long tradeId, Long buyerId, String reason) {
        Trade trade = tradeRepository.findByIdWithLock(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        validateBuyer(trade, buyerId);
        validateDisputable(trade);

        // 양방향
        if (trade.getTradeType() == TradeType.SWAP) {
            List<Trade> trades = tradeRepository.findAllByTradeGroupId(trade.getTradeGroupId());
            for (Trade t : trades) {
                t.dispute(); // 거래 상태 변경 (UNDER_REVIEW -> DISPUTED)

                Escrow escrow = escrowRepository.findByTradeId(t.getId())
                        .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
                escrow.freeze(reason); // 에스크로 상태 변경 (HELD -> FROZEN)
            }

            Escrow requesterEscrow = escrowRepository.findByTradeId(tradeId)
                    .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
            return TradeRes.of(trade, requesterEscrow);

        }
        // 단방향
        else {
            Escrow escrow = escrowRepository.findByTradeId(tradeId)
                    .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

            trade.dispute();
            escrow.freeze(reason);

            return TradeRes.of(trade, escrow);
        }
    }

    @Transactional
    public TradeRes resolveDispute(Long tradeId, DisputeVerdict verdict) {
        Objects.requireNonNull(verdict, "verdict값은 null이 될 수 없습니다.");
        Trade trade = tradeRepository.findByIdWithLock(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        if (trade.getStatus() != com.back.baton.domain.trade.entity.TradeStatus.DISPUTED) {
            throw new CustomException(TradeErrorCode.TRADE_NOT_DISPUTED);
        }

        Escrow escrow = escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));

        // 구매자 승소 -> 거래 취소 + 에스크로 환불
        if (verdict == DisputeVerdict.BUYER_WIN) {
            trade.cancel(); // 거래 상태 변경 (UNDER_REVIEW -> CANCELLED)
            escrow.refundFrozen(); // 에스크로 상태 변경 (FROZEN -> REFUNDED)
            creditService.refundFromEscrow(
                    escrow.getPayerId(),
                    escrow.getAmount(),
                    tradeId
            );
        }
        // 판매자 승소 -> 거래 완료 + 에스크로 정산
        else {
            trade.complete(); // 거래 상태 변경 (UNDER_REVIEW -> COMPLETED)
            escrow.releaseFrozen(); // 에스크로 상태 변경 (FROZEN -> RELEASED)
            creditService.settleEscrow(
                    escrow.getPayerId(),
                    escrow.getPayeeId(),
                    escrow.getAmount(),
                    escrow.getSettlementAmount(),
                    tradeId
            );
        }

        return TradeRes.of(trade, escrow);
    }

    public List<DisputeRes> getDisputedTrades() {
        List<Trade> trades = tradeRepository.findAllByStatus(TradeStatus.DISPUTED);

        if (trades.isEmpty()) {
            return List.of();
        }

        List<Long> tradeIds = trades.stream().map(Trade::getId).toList();
        Map<Long, Escrow> escrowByTradeId = escrowRepository.findAllByTradeIdIn(tradeIds).stream()
                .collect(Collectors.toMap(Escrow::getTradeId, e -> e));

        return trades.stream()
                .filter(t -> escrowByTradeId.containsKey(t.getId()))
                .map(t -> DisputeRes.of(t, escrowByTradeId.get(t.getId())))
                .toList();
    }

    private void validateTradeParticipant(Trade trade, Long userId) {
        if (!Objects.equals(trade.getBuyerId(), userId) && !Objects.equals(trade.getSellerId(), userId)) {
            throw new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    private void validateCancellable(Trade trade) {
        switch (trade.getStatus()) {
            case UNDER_REVIEW -> throw new CustomException(TradeErrorCode.TRADE_UNDER_REVIEW);
            case COMPLETED, AWAITING_PARTNER -> throw new CustomException(TradeErrorCode.TRADE_ALREADY_COMPLETED);
            case CANCELLED -> throw new CustomException(TradeErrorCode.TRADE_ALREADY_CANCELLED);
            case DISPUTED -> throw new CustomException(TradeErrorCode.TRADE_IN_DISPUTE);
            case IN_PROGRESS -> throw new CustomException(TradeErrorCode.TRADE_ALREADY_IN_PROGRESS);
            default -> throw new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    private void validateBuyer(Trade trade, Long buyerId) {
        if (!Objects.equals(trade.getBuyerId(), buyerId)) {
            throw new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    private void validateUnderReview(Trade trade) {
        if (trade.getStatus() != TradeStatus.UNDER_REVIEW) {
            throw new CustomException(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);
        }
    }

    public CursorPageRes<TradeListRes> getMyTrades(Long userId, TradeStatus status, Long cursor, int size) {
        List<TradeListRes> rows = tradeRepository.findMyTrades(userId, status, cursor, size);
        return CursorPageRes.from(rows, size, TradeListRes::tradeId);
    }

    private void validateDisputable(Trade trade) {
        switch (trade.getStatus()) {
            case UNDER_REVIEW -> {
            } // 검토 중인 거래만 분쟁 신청 가능
            case DISPUTED -> throw new CustomException(TradeErrorCode.TRADE_ALREADY_DISPUTED);
            default -> throw new CustomException(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);
        }
    }
}