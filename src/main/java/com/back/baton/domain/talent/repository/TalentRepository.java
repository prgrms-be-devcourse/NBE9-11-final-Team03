package com.back.baton.domain.talent.repository;

import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.entity.TalentStatus;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TalentRepository extends JpaRepository<Talent, Long>, TalentRepositoryCustom {

    // category를 fetch join으로 함께 조회한다.
    @Query("""
            select t, u
            from Talent t
            join fetch t.category
            join User u on u.id = t.authorId
            where t.id = :talentId and t.deletedAt is null
            """)
    List<Object[]> findDetailById(@Param("talentId") Long talentId);

    // 조회수를 1 증가시킨다.
    @Modifying(clearAutomatically = true)
    @Query("update Talent t set t.viewCount = t.viewCount + 1 where t.id = :talentId and t.deletedAt is null")
    int increaseViewCount(@Param("talentId") Long talentId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Talent t SET t.title='', t.content='', t.estimatedHours=0, t.creditPrice=0,
                        t.status='CLOSED', t.viewCount=0, t.completeCount=0, t.avgRating=0.00, t.deletedAt = :now
            WHERE t.authorId = :authorId
            """)
    void deleteTalentByUserId(@Param("authorId") Long authorId, @Param("now") LocalDateTime now);

    // 삭제되지 않은 본인 재능 개수 집계.
    int countByAuthorIdAndDeletedAtIsNull(Long authorId);

    Optional<Talent> findByIdAndDeletedAtIsNull(Long id);

    // 관리자 재능 목록 조회 필터 검색.
    @Query(value = """
            SELECT t
            FROM Talent t
            JOIN FETCH t.category
            WHERE t.deletedAt IS NULL
              AND (:status IS NULL OR t.status = :status)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(t)
            FROM Talent t
            WHERE t.deletedAt IS NULL
              AND (:status IS NULL OR t.status = :status)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Talent> searchAdminTalents(
            @Param("status") TalentStatus status,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 관리자 대시보드 재능 전체 수 집계. 삭제된 재능은 제외한다.
    long countByDeletedAtIsNull();

    // 관리자 대시보드 재능 상태별 수 집계. 삭제된 재능은 제외한다.
    long countByStatusAndDeletedAtIsNull(TalentStatus status);

    default Talent getActiveTalentOrThrow(Long id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(TalentErrorCode.TALENT_NOT_FOUND));
    }

    @Query("""
        select new com.back.baton.domain.talent.dto.response.TalentListRes(
            t.id, t.category.name, t.title, t.creditPrice, t.estimatedHours,
            t.avgRating, t.completeCount, t.viewCount, t.createdAt)
        from Talent t
        where t.authorId = :authorId and t.deletedAt is null
        order by t.id desc
        """)
    List<TalentListRes> findMyTalents(@Param("authorId") Long authorId);
}
