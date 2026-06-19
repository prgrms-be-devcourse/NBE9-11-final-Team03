package com.back.baton.domain.escrow.repository;

import com.back.baton.domain.escrow.entity.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {

    Optional<Escrow> findByTradeId(Long tradeId);
}