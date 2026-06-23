package com.back.baton.domain.talent.repository;

import com.back.baton.domain.talent.entity.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TalentRepository extends JpaRepository<Talent, Long>, TalentRepositoryCustom {

    // category는 fetch join으로 같이 적재
    // @ManyToOne 전환 시 join fetch t.author로 교체
    @Query("""
            select t, u
            from Talent t
            join fetch t.category
            join User u on u.id = t.authorId
            where t.id = :talentId and t.deletedAt is null
            """)
    List<Object[]> findDetailById(@Param("talentId") Long talentId);

    // 조회수 +1 삭제글 무시
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

    // 삭제되지 않은 본인 재능 개수 (등록 제한 검사용)
    int countByAuthorIdAndDeletedAtIsNull(Long authorId);
}