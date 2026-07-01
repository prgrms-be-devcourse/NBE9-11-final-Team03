package com.back.baton.domain.trade.scheduler;

import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.trade.service.TradeService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TradeErrorCode;
import com.back.baton.global.slack.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeScheduler {

    private final TradeRepository tradeRepository;
    private final TradeService tradeService;
    private final SlackService slackService;

    // 매 정각 실행
    @Scheduled(cron = "0 0 * * * *")
    public void autoConfirmExpiredTrades() {
        log.info("만료된 거래에 대한 자동 구매 확정 검사를 시작합니다...");
        LocalDateTime now = LocalDateTime.now();
        List<Trade> expiredTrades = tradeRepository.findExpiredUnderReviewTrades(now);

        // 만료된 거래 목록을 하나씩 순회하며 자동 확정 처리 및 결과 알림 발송
        for (Trade trade : expiredTrades) {
            try {
                // 정상 처리 성공 사례
                // 대리 구매 확정 및 에스크로 정산 비즈니스 로직 실행
                tradeService.autoConfirm(trade.getId());
                log.info("거래 ID {}의 자동 구매 확정을 성공적으로 완료했습니다.", trade.getId());

                // 슬랙 채널로 성공 알림 전송 (비동기)
                slackService.sendNotification(String.format(
                        "✅ *[자동 구매 확정 완료]*\n- 거래 ID: %d\n- 구매자 ID: %d\n- 판매자 ID: %d\n- 거래 대금: %d 크레딧\n- 7일간 구매 확정이 발생하지 않아 시스템에서 자동 정산 처리되었습니다.",
                        trade.getId(), trade.getBuyerId(), trade.getSellerId(), trade.getCreditPrice()
                ));
            } catch (CustomException e) {
                // 이미 수동 확정/취소 등으로 상태가 바뀐 경우
                if (e.getErrorCode() == TradeErrorCode.TRADE_NOT_UNDER_REVIEW) {
                    log.info("거래 ID {}는 이미 처리된 상태입니다. 자동 확정 건너뜀.", trade.getId());
                }
                // 그 외 예외 상황
                else {
                    log.error("거래 ID {}의 자동 구매 확정 실패", trade.getId(), e);
                    slackService.sendNotification(String.format(
                            "⚠️ *[자동 구매 확정 실패]*\n- 거래 ID: %d\n- 구매자 ID: %d\n- 판매자 ID: %d\n- 실패 사유: %s",
                            trade.getId(), trade.getBuyerId(), trade.getSellerId(), e.getMessage()
                    ));
                }
            } catch (Exception e) {
                // 예상치 못한 런타임 오류
                log.error("거래 ID {}의 자동 구매 확정 중 예상치 못한 오류 발생", trade.getId(), e);
                slackService.sendNotification(String.format(
                        "⚠️ *[자동 구매 확정 실패]*\n- 거래 ID: %d\n- 구매자 ID: %d\n- 판매자 ID: %d\n- 실패 사유: %s",
                        trade.getId(), trade.getBuyerId(), trade.getSellerId(), e.getMessage()
                ));
            }
        }

        // 전체 배치 수행 완료 로그 출력
        log.info("자동 구매 확정 검사가 완료되었습니다. 처리된 거래 수: {}개", expiredTrades.size());
    }
}