package com.back.baton.domain.user.repository;

import com.back.baton.domain.user.entity.WithdrawnUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface WithdrawnUserRepository extends JpaRepository<WithdrawnUser,Long> {
    void deleteByCreatedAtBeforeAndPermanentBanIsFalse(LocalDateTime thresholdDate);
}
