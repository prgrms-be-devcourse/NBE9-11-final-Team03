package com.back.baton.domain.trade.repository;

import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trade t WHERE t.id = :id")
    Optional<Trade> findByIdWithLock(@Param("id") Long id);

    boolean existsByTalentIdAndStatus(Long talentId, TradeStatus status);
}