package com.back.baton.domain.profile.repository;

import com.back.baton.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);

    @Query("""
        SELECT p from Profile p
        JOIN FETCH p.user
        WHERE p.user.id = :userId
    """)
    Optional<Profile> findDetailByUserId(@Param("userId") Long userId);
}
