package com.back.baton.domain.trade.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.request.TradeSubmissionReq;
import com.back.baton.domain.trade.dto.response.PresignedUrlRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.dto.response.TradeSubmissionRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeSubmission;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.trade.repository.TradeSubmissionRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.S3ErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import com.back.baton.global.s3.S3Service;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeSubmissionService {

    private final TradeRepository tradeRepository;
    private final EscrowRepository escrowRepository;
    private final TradeSubmissionRepository tradeSubmissionRepository;
    private final S3Service s3Service;
    private final CreditService creditService;

    public TradeSubmissionRes getSubmission(Long tradeId, Long buyerId) {
        Trade trade = getTrade(tradeId);
        validateBuyer(trade, buyerId);
        validateUnderReview(trade);

        Escrow escrow = getEscrow(tradeId);
        TradeSubmission submission = tradeSubmissionRepository.findByEscrowId(escrow.getId())
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_SUBMISSION_NOT_FOUND));

        String fileUrl = s3Service.generatePresignedGetUrl(submission.getFileKey());
        return TradeSubmissionRes.of(submission, fileUrl);
    }

    @Transactional
    public TradeRes confirmPurchase(Long tradeId, Long buyerId) {
        Trade trade = tradeRepository.findByIdWithLock(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));

        validateBuyer(trade, buyerId);
        validateUnderReview(trade);

        Escrow escrow = getEscrow(tradeId);

        escrow.release(); // 에스크로 상태 변경 (HELD -> RELEASED)
        trade.complete(); // 거래 상태 변경 (UNDER_REVIEW -> COMPLETED)

        // 크레딧 정산
        creditService.settleEscrow(
                escrow.getPayerId(),
                escrow.getPayeeId(),
                escrow.getAmount(),
                escrow.getSettlementAmount(),
                tradeId,
                "TRADE-SETTLE-" + tradeId
        );

        return TradeRes.of(trade, escrow);
    }

    public PresignedUrlRes getPresignedUrl(Long tradeId, Long sellerId, String fileName) {
        Trade trade = getTrade(tradeId);
        validateSeller(trade, sellerId);
        validateInProgress(trade);

        String fileKey = buildFileKey(tradeId, fileName);
        String presignedUrl = s3Service.generatePresignedPutUrl(fileKey);

        return new PresignedUrlRes(presignedUrl, fileKey);
    }

    @Transactional
    public TradeSubmissionRes submitResult(Long tradeId, Long sellerId, TradeSubmissionReq req) {
        Trade trade = getTrade(tradeId);

        validateSeller(trade, sellerId);
        validateInProgress(trade);

        validateFileKeyBelongsToTrade(tradeId, req.fileKey()); // fileKey 경로 검증

        Escrow escrow = getEscrow(tradeId);

        TradeSubmission submission = TradeSubmission.create(escrow.getId(), req.fileKey(), req.description());
        tradeSubmissionRepository.save(submission);

        trade.submitResult(); // Trade 상태 변경

        String fileUrl = s3Service.generatePresignedGetUrl(req.fileKey());
        return TradeSubmissionRes.of(submission, fileUrl);
    }

    private Trade getTrade(Long tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(TradeErrorCode.TRADE_NOT_FOUND));
    }

    private Escrow getEscrow(Long tradeId) {
        return escrowRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(EscrowErrorCode.ESCROW_NOT_FOUND));
    }

    private void validateBuyer(Trade trade, Long buyerId) {
        if (!Objects.equals(trade.getBuyerId(), buyerId)) {
            throw new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    private void validateSeller(Trade trade, Long sellerId) {
        if (!Objects.equals(trade.getSellerId(), sellerId)) {
            throw new CustomException(TradeErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    private void validateInProgress(Trade trade) {
        if (trade.getStatus() != TradeStatus.IN_PROGRESS) {
            throw new CustomException(TradeErrorCode.TRADE_NOT_IN_PROGRESS);
        }
    }

    private void validateUnderReview(Trade trade) {
        if (trade.getStatus() != TradeStatus.UNDER_REVIEW) {
            throw new CustomException(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);
        }
    }

    private void validateFileKeyBelongsToTrade(Long tradeId, String fileKey) {
        String expectedPrefix = "trades/" + tradeId + "/";
        if (!fileKey.startsWith(expectedPrefix)) {
            throw new CustomException(S3ErrorCode.INVALID_FILE_KEY);
        }
    }

    private String buildFileKey(Long tradeId, String fileName) {
        String extension = extractExtension(fileName);
        return "trades/" + tradeId + "/" + UUID.randomUUID() + "." + extension;
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "bin";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}