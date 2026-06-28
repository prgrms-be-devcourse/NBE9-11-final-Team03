package com.back.baton.domain.admin.service;

import com.back.baton.domain.admin.dto.response.AdminDashboardSummaryRes;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.domain.talent.repository.TalentReportRepository;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final TalentRepository talentRepository;
    private final TradeRepository tradeRepository;
    private final TalentReportRepository talentReportRepository;
    private final EscrowRepository escrowRepository;

    // 관리자 첫 화면에서 사용할 전체/상태별 요약 수치를 집계한다.
    public AdminDashboardSummaryRes getDashboardSummary() {
        return new AdminDashboardSummaryRes(
                userRepository.count(),
                countStatuses(UserStatus.values(), userRepository::countByStatus),
                talentRepository.countByDeletedAtIsNull(),
                countStatuses(TalentStatus.values(), talentRepository::countByStatusAndDeletedAtIsNull),
                tradeRepository.count(),
                countStatuses(TradeStatus.values(), tradeRepository::countByStatus),
                talentReportRepository.count(),
                countStatuses(ReportStatus.values(), talentReportRepository::countByStatus),
                escrowRepository.count(),
                countStatuses(EscrowStatus.values(), escrowRepository::countByStatus)
        );
    }

    private <E extends Enum<E>> Map<String, Long> countStatuses(E[] statuses, Function<E, Long> counter) {
        return Arrays.stream(statuses)
                .collect(Collectors.toMap(Enum::name, counter));
    }
}
