package com.back.baton.domain.credit.repository;

import com.back.baton.domain.credit.entity.CreditAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditAccountRepository extends JpaRepository<CreditAccount, Long> {

    Optional<CreditAccount> findByUserId(Long userId);
}