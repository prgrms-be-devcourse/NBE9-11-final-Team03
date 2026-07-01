package com.back.baton.global.slack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
public class SlackService {

    @Value("${slack.webhook-url:}")
    private String webhookUrl;

    // 재사용 가능한 HTTP 클라이언트 생성
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

    public void sendNotification(String message) {
        if (webhookUrl == null || webhookUrl.isBlank() || webhookUrl.startsWith("${")) {
            log.warn("슬랙 Webhook URL이 설정되지 않았습니다. 알림 전송을 건너뜁니다.");
            return;
        }

        try {
            String jsonBody = "{\"text\": \"" + escapeJson(message) + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // 비동기로 발송하여 스케줄러 스레드가 응답 대기로 멈추는 것을 방지
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 400) {
                            log.error("슬랙 알림 전송 실패. 상태 코드: {}, 응답 본문: {}",
                                    response.statusCode(), response.body());
                        } else {
                            log.info("슬랙 알림을 성공적으로 전송했습니다.");
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("슬랙 알림 전송 중 에러 발생", ex);
                        return null;
                    });

        } catch (Exception e) {
            log.error("슬랙 알림 전송 준비 실패", e);
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}