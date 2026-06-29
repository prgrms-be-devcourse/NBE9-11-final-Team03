package com.back.baton.domain.trade.repository;

import com.back.baton.domain.trade.entity.Trade;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.entity.TradeType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long>, TradeRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trade t WHERE t.id = :id")
    Optional<Trade> findByIdWithLock(@Param("id") Long id);

    boolean existsByTalentIdAndStatusIn(Long talentId, List<TradeStatus> statuses);

    List<Trade> findAllByStatus(TradeStatus status);

    List<Trade> findAllByTradeGroupId(Long tradeGroupId);

    // 관리자 대시보드 거래 상태별 수 집계.
    long countByStatus(TradeStatus status);

    @Query("SELECT t FROM Trade t " +
            "JOIN Escrow e ON e.tradeId = t.id " +
            "WHERE t.status = com.back.baton.domain.trade.entity.TradeStatus.UNDER_REVIEW " +
            "AND e.expiresAt <= :now")
    List<Trade> findExpiredUnderReviewTrades(@Param("now") LocalDateTime now);
}
    // 관리자 거래 목록 조회 필터 검색.
    @Query("""
            SELECT t
            FROM Trade t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:buyerId IS NULL OR t.buyerId = :buyerId)
              AND (:sellerId IS NULL OR t.sellerId = :sellerId)
              AND (:tradeType IS NULL OR t.tradeType = :tradeType)
            """)
    Page<Trade> searchAdminTrades(
            @Param("status") TradeStatus status,
            @Param("buyerId") Long buyerId,
            @Param("sellerId") Long sellerId,
            @Param("tradeType") TradeType tradeType,
            Pageable pageable
    );
}
