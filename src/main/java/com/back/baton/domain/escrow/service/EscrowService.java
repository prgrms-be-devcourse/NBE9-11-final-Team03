package com.back.baton.domain.escrow.service;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EscrowService {

    private final EscrowRepository escrowRepository;

    @Value("${escrow.confirmation-expiry-days}")
    private int confirmationExpiryDays;

    @Value("${escrow.fee-rate}")
    private double feeRate;

    public Escrow create(Long tradeId, Long payerId, Long payeeId, Integer amount) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(confirmationExpiryDays);

        Escrow escrow = Escrow.createHeld(tradeId, payerId, payeeId, amount, feeRate, expiresAt);

        return escrowRepository.save(escrow);
    }
}