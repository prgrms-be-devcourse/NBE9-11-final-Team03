package com.back.baton.domain.escrow.service;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EscrowService {

    private final EscrowRepository escrowRepository;

    public Escrow create(Long tradeId, Long payerId, Long payeeId, Integer amount) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7); // 7일 후 자동 확정
        Escrow escrow = Escrow.createHeld(tradeId, payerId, payeeId, amount, expiresAt);
        return escrowRepository.save(escrow);
    }
}