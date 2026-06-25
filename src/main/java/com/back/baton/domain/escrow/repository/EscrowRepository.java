package com.back.baton.domain.escrow.repository;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {

    Optional<Escrow> findByTradeId(Long tradeId);

    List<Escrow> findAllByTradeIdIn(List<Long> tradeIds);
    @Query("""
    SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
            FROM Escrow e
            WHERE(e.payerId = :payerId OR e.payeeId = :payeeId)
                AND e.status IN :statuses
    """)
    boolean existsByUserIdAndStatus(@Param("payerId") Long payerId, @Param("payeeId") Long payeeId, @Param("statuses") List<EscrowStatus> statuses);
}