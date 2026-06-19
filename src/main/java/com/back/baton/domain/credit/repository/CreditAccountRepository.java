package com.back.baton.domain.credit.repository;

import com.back.baton.domain.credit.entity.CreditAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CreditAccountRepository extends JpaRepository<CreditAccount, Long> {

    Optional<CreditAccount> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CreditAccount c SET c.balance = c.balance + :amount WHERE c.userId = :userId")
    int addBalance(@Param("userId") Long userId, @Param("amount") int amount);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CreditAccount c SET c.balance = c.balance - :amount WHERE c.userId = :userId AND c.balance >= :amount")
    int deductBalance(@Param("userId") Long userId, @Param("amount") int amount);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CreditAccount c SET c.balance = c.balance - :amount, c.escrowBalance = c.escrowBalance + :amount WHERE c.userId = :userId AND c.balance >= :amount")
    int holdForEscrow(@Param("userId") Long userId, @Param("amount") int amount);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CreditAccount c SET c.escrowBalance = c.escrowBalance - :amount, c.balance = c.balance + :amount WHERE c.userId = :userId AND c.escrowBalance >= :amount")
    int releaseEscrow(@Param("userId") Long userId, @Param("amount") int amount);
}