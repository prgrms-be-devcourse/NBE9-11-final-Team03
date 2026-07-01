package com.back.baton.domain.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "관리자 대시보드 요약 응답")
public record AdminDashboardSummaryRes(
        @Schema(description = "전체 유저 수", example = "120")
        long totalUsers,

        @Schema(description = "유저 상태별 수", example = "{\"ACTIVE\":100,\"SUSPENDED\":5}")
        Map<String, Long> usersByStatus,

        @Schema(description = "전체 재능 수. 삭제된 재능은 제외합니다.", example = "80")
        long totalTalents,

        @Schema(description = "재능 상태별 수. 삭제된 재능은 제외합니다.", example = "{\"ACTIVE\":70,\"CLOSED\":10}")
        Map<String, Long> talentsByStatus,

        @Schema(description = "전체 거래 수", example = "45")
        long totalTrades,

        @Schema(description = "거래 상태별 수", example = "{\"IN_PROGRESS\":10,\"COMPLETED\":30}")
        Map<String, Long> tradesByStatus,

        @Schema(description = "전체 신고 수", example = "12")
        long totalReports,

        @Schema(description = "신고 상태별 수", example = "{\"PENDING\":3,\"RESOLVED\":9}")
        Map<String, Long> reportsByStatus,

        @Schema(description = "전체 에스크로 수", example = "40")
        long totalEscrows,

        @Schema(description = "에스크로 상태별 수", example = "{\"HELD\":5,\"RELEASED\":30}")
        Map<String, Long> escrowsByStatus
) {
}
