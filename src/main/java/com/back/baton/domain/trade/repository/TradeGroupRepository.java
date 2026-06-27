package com.back.baton.domain.trade.repository;

import com.back.baton.domain.trade.entity.TradeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TradeGroupRepository extends JpaRepository<TradeGroup, Long> {
    Optional<TradeGroup> findByMatchProposalId(Long matchProposalId);
}