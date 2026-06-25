package com.back.baton.domain.profile.repository;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);

    @Query("""
        SELECT p from Profile p
        JOIN FETCH p.user
        WHERE p.user.id = :userId
    """)
    Optional<Profile> findWithUserByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT link
        FROM Profile p
        JOIN p.portfolioLinkList link
        WHERE p.user.id = :userId
    """)
    List<String> findPortfolioLinksByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT c
        FROM Profile p
        JOIN p.myTalentCategories c
        WHERE p.user.id = :userId
        ORDER BY c.sortOrder ASC
    """)
    List<Category> findMyTalentCategoriesByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT c
        FROM Profile p
        JOIN p.wantTalentCategories c
        WHERE p.user.id = :userId
        ORDER BY c.sortOrder ASC
    """)
    List<Category> findWantTalentCategoriesByUserId(@Param("userId") Long userId);
}
