package com.back.baton.domain.credit.repository;

import com.back.baton.domain.credit.entity.CreditTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long>, CreditTransactionRepositoryCustom {

    // 관리자 유저 크레딧 거래 내역 조회.
    Page<CreditTransaction> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
}
