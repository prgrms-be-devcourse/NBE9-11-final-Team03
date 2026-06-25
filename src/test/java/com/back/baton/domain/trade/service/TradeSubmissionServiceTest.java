package com.back.baton.domain.trade.service;

import com.back.baton.domain.credit.service.CreditService;
import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.trade.dto.request.TradeSubmissionReq;
import com.back.baton.domain.trade.dto.response.PresignedUrlRes;
import com.back.baton.domain.trade.dto.response.TradeRes;
import com.back.baton.domain.trade.dto.response.TradeSubmissionRes;
import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeSubmission;
import com.back.baton.domain.trade.entity.TradeType;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.trade.repository.TradeSubmissionRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.EscrowErrorCode;
import com.back.baton.global.response.code.S3ErrorCode;
import com.back.baton.global.response.code.TradeErrorCode;
import com.back.baton.global.s3.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TradeSubmissionServiceTest {

    @InjectMocks
    private TradeSubmissionService tradeSubmissionService;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private TradeSubmissionRepository tradeSubmissionRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private CreditService creditService;

    @Test
    @DisplayName("결과물 확인 성공 - 제출 내역과 Presigned GET URL을 반환한다")
    void getSubmission_success() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);
        Escrow escrow = createEscrow(buyerId, 3L);

        TradeSubmission submission = TradeSubmission.create(escrow.getId(), "trades/1/uuid.pdf", "결과물 설명");
        ReflectionTestUtils.setField(submission, "id", 10L);

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.of(escrow));
        given(tradeSubmissionRepository.findByEscrowId(escrow.getId())).willReturn(Optional.of(submission));
        given(s3Service.generatePresignedGetUrl("trades/1/uuid.pdf")).willReturn("https://s3.example.com/get");

        TradeSubmissionRes result = tradeSubmissionService.getSubmission(tradeId, buyerId);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.fileUrl()).isEqualTo("https://s3.example.com/get");
        assertThat(result.description()).isEqualTo("결과물 설명");
    }

    @Test
    @DisplayName("결과물 확인 - 존재하지 않는 거래이면 TRADE_NOT_FOUND 예외가 발생한다")
    void getSubmission_tradeNotFound() {
        given(tradeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeSubmissionService.getSubmission(999L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        then(escrowRepository).should(never()).findByTradeId(any());
    }

    @Test
    @DisplayName("결과물 확인 - 구매자가 아니면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void getSubmission_accessDenied() {
        Long tradeId = 1L;
        Trade trade = createTrade(2L, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.getSubmission(tradeId, 999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        then(escrowRepository).should(never()).findByTradeId(any());
    }

    @Test
    @DisplayName("결과물 확인 - UNDER_REVIEW가 아닌 거래이면 TRADE_NOT_UNDER_REVIEW 예외가 발생한다")
    void getSubmission_notUnderReview() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.getSubmission(tradeId, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);

        then(escrowRepository).should(never()).findByTradeId(any());
    }

    @Test
    @DisplayName("결과물 확인 - 에스크로가 없으면 ESCROW_NOT_FOUND 예외가 발생한다")
    void getSubmission_escrowNotFound() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeSubmissionService.getSubmission(tradeId, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);

        then(tradeSubmissionRepository).should(never()).findByEscrowId(any());
    }

    @Test
    @DisplayName("결과물 확인 - 제출 내역이 없으면 TRADE_SUBMISSION_NOT_FOUND 예외가 발생한다")
    void getSubmission_submissionNotFound() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);
        Escrow escrow = createEscrow(buyerId, 3L);

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.of(escrow));
        given(tradeSubmissionRepository.findByEscrowId(escrow.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeSubmissionService.getSubmission(tradeId, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_SUBMISSION_NOT_FOUND);

        then(s3Service).should(never()).generatePresignedGetUrl(anyString());
    }

    @Test
    @DisplayName("Presigned URL 발급 성공 - presignedUrl과 fileKey를 반환한다")
    void getPresignedUrl_success() {
        Long tradeId = 1L;
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));
        given(s3Service.generatePresignedPutUrl(anyString())).willReturn("https://s3.example.com/presigned");

        PresignedUrlRes result = tradeSubmissionService.getPresignedUrl(tradeId, sellerId, "result.pdf");

        assertThat(result.presignedUrl()).isEqualTo("https://s3.example.com/presigned");
        assertThat(result.fileKey()).contains("trades/1/");
        assertThat(result.fileKey()).endsWith(".pdf");
    }

    @Test
    @DisplayName("Presigned URL 발급 - 존재하지 않는 거래이면 TRADE_NOT_FOUND 예외가 발생한다")
    void getPresignedUrl_tradeNotFound() {
        given(tradeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeSubmissionService.getPresignedUrl(999L, 3L, "result.pdf"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        then(s3Service).should(never()).generatePresignedPutUrl(anyString());
    }

    @Test
    @DisplayName("Presigned URL 발급 - 판매자가 아니면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void getPresignedUrl_accessDenied() {
        Long tradeId = 1L;
        Long outsiderId = 999L;
        Trade trade = createTrade(2L, 3L);

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.getPresignedUrl(tradeId, outsiderId, "result.pdf"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        then(s3Service).should(never()).generatePresignedPutUrl(anyString());
    }

    @Test
    @DisplayName("Presigned URL 발급 - IN_PROGRESS가 아닌 거래이면 TRADE_NOT_IN_PROGRESS 예외가 발생한다")
    void getPresignedUrl_notInProgress() {
        Long tradeId = 1L;
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.getPresignedUrl(tradeId, sellerId, "result.pdf"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_IN_PROGRESS);

        then(s3Service).should(never()).generatePresignedPutUrl(anyString());
    }

    @Test
    @DisplayName("결과물 제출 성공 - 제출 내역이 저장되고 Trade 상태가 UNDER_REVIEW로 변경된다")
    void submitResult_success() {
        Long tradeId = 1L;
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        Escrow escrow = createEscrow(2L, sellerId);
        TradeSubmissionReq req = new TradeSubmissionReq("trades/1/uuid.pdf", "결과물 설명");

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.of(escrow));
        given(tradeSubmissionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(s3Service.generatePresignedGetUrl(anyString())).willReturn("https://s3.example.com/get");

        TradeSubmissionRes result = tradeSubmissionService.submitResult(tradeId, sellerId, req);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.UNDER_REVIEW);
        assertThat(result.fileUrl()).isEqualTo("https://s3.example.com/get");
        assertThat(result.description()).isEqualTo("결과물 설명");

        ArgumentCaptor<TradeSubmission> captor = ArgumentCaptor.forClass(TradeSubmission.class);
        then(tradeSubmissionRepository).should().save(captor.capture());
        assertThat(captor.getValue().getFileKey()).isEqualTo("trades/1/uuid.pdf");
    }

    @Test
    @DisplayName("결과물 제출 - 존재하지 않는 거래이면 TRADE_NOT_FOUND 예외가 발생한다")
    void submitResult_tradeNotFound() {
        TradeSubmissionReq req = new TradeSubmissionReq("trades/1/uuid.pdf", "설명");

        given(tradeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeSubmissionService.submitResult(999L, 3L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        then(tradeSubmissionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("결과물 제출 - 판매자가 아니면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void submitResult_accessDenied() {
        Long tradeId = 1L;
        Long outsiderId = 999L;
        Trade trade = createTrade(2L, 3L);
        TradeSubmissionReq req = new TradeSubmissionReq("trades/1/uuid.pdf", "설명");

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.submitResult(tradeId, outsiderId, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        then(tradeSubmissionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("결과물 제출 - IN_PROGRESS가 아닌 거래이면 TRADE_NOT_IN_PROGRESS 예외가 발생한다")
    void submitResult_notInProgress() {
        Long tradeId = 1L;
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);
        TradeSubmissionReq req = new TradeSubmissionReq("trades/1/uuid.pdf", "설명");

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.submitResult(tradeId, sellerId, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_IN_PROGRESS);

        then(tradeSubmissionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("결과물 제출 - fileKey가 해당 거래 경로로 시작하지 않으면 INVALID_FILE_KEY 예외가 발생한다")
    void submitResult_invalidFileKey() {
        Long tradeId = 1L;
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        TradeSubmissionReq req = new TradeSubmissionReq("trades/999/uuid.pdf", "설명");

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.submitResult(tradeId, sellerId, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(S3ErrorCode.INVALID_FILE_KEY);

        then(escrowRepository).should(never()).findByTradeId(any());
        then(tradeSubmissionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("결과물 제출 - 에스크로가 없으면 ESCROW_NOT_FOUND 예외가 발생한다")
    void submitResult_escrowNotFound() {
        Long tradeId = 1L;
        Long sellerId = 3L;
        Trade trade = createTrade(2L, sellerId);
        TradeSubmissionReq req = new TradeSubmissionReq("trades/1/uuid.pdf", "설명");

        given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeSubmissionService.submitResult(tradeId, sellerId, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);

        then(tradeSubmissionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("구매 확정 성공 - Trade가 COMPLETED, Escrow가 RELEASED로 변경된다")
    void confirmPurchase_success() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);
        Escrow escrow = createEscrow(buyerId, 3L);

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.of(escrow));

        TradeRes result = tradeSubmissionService.confirmPurchase(tradeId, buyerId);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(result.tradeStatus()).isEqualTo(TradeStatus.COMPLETED);
    }

    @Test
    @DisplayName("구매 확정 - 존재하지 않는 거래이면 TRADE_NOT_FOUND 예외가 발생한다")
    void confirmPurchase_tradeNotFound() {
        given(tradeRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeSubmissionService.confirmPurchase(999L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);
    }

    @Test
    @DisplayName("구매 확정 - 구매자가 아니면 TRADE_ACCESS_DENIED 예외가 발생한다")
    void confirmPurchase_accessDenied() {
        Long tradeId = 1L;
        Trade trade = createTrade(2L, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.confirmPurchase(tradeId, 999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ACCESS_DENIED);

        then(escrowRepository).should(never()).findByTradeId(any());
    }

    @Test
    @DisplayName("구매 확정 - UNDER_REVIEW가 아닌 거래이면 TRADE_NOT_UNDER_REVIEW 예외가 발생한다")
    void confirmPurchase_notUnderReview() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));

        assertThatThrownBy(() -> tradeSubmissionService.confirmPurchase(tradeId, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_UNDER_REVIEW);

        then(escrowRepository).should(never()).findByTradeId(any());
    }

    @Test
    @DisplayName("구매 확정 - 에스크로가 없으면 ESCROW_NOT_FOUND 예외가 발생한다")
    void confirmPurchase_escrowNotFound() {
        Long tradeId = 1L;
        Long buyerId = 2L;
        Trade trade = createTrade(buyerId, 3L);
        ReflectionTestUtils.setField(trade, "status", TradeStatus.UNDER_REVIEW);

        given(tradeRepository.findByIdWithLock(tradeId)).willReturn(Optional.of(trade));
        given(escrowRepository.findByTradeId(tradeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tradeSubmissionService.confirmPurchase(tradeId, buyerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(EscrowErrorCode.ESCROW_NOT_FOUND);
    }

    private Trade createTrade(Long buyerId, Long sellerId) {
        Trade trade = Trade.create(1L, 10L, buyerId, sellerId, 5000, TradeType.PURCHASE);
        ReflectionTestUtils.setField(trade, "id", 1L);
        return trade;
    }

    private Escrow createEscrow(Long payerId, Long payeeId) {
        Escrow escrow = Escrow.createHeld(1L, payerId, payeeId, 5000, 500, 4500, LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(escrow, "id", 1L);
        return escrow;
    }
}