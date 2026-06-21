package com.back.baton.domain.user.repository;

import com.back.baton.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByTokenValue(String refreshTokenValue);

    @Modifying
    @Query("delete from RefreshToken r where r.userId = :userId")
    void deleteByUserIdCustom(@Param("userId") Long userId);
}
