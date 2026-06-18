package com.back.baton.domain.escrow.repository;

import com.back.baton.domain.escrow.entity.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {
}