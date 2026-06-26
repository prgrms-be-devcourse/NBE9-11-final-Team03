package com.back.baton.domain.credit.repository;

import com.back.baton.domain.credit.entity.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long>, CreditTransactionRepositoryCustom {

}