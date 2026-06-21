package com.back.baton.domain.trade.repository;

import com.back.baton.domain.trade.entity.TradeSubmission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeSubmissionRepository extends JpaRepository<TradeSubmission, Long> {

    Optional<TradeSubmission> findByEscrowId(Long escrowId);
}
