package com.back.baton.domain.trade.repository;

import com.back.baton.domain.trade.entity.TradeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeGroupRepository extends JpaRepository<TradeGroup, Long> {
}