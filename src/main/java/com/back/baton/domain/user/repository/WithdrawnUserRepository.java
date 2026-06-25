package com.back.baton.domain.user.repository;

import com.back.baton.domain.user.entity.WithdrawnUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WithdrawnUserRepository extends JpaRepository<WithdrawnUser,Long> {
    @Modifying(clearAutomatically = true)
    @Query("""
    DELETE from WithdrawnUser w
    WHERE w.createdAt < :now AND w.permanentBan = false
    """)
    void deleteByCreatedAtBeforeAndPermanentBanIsFalse(@Param("now")LocalDateTime now);
    boolean existsByEncodedEmail(String encodedEmail);
}
