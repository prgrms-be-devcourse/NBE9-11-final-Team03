package com.back.baton.domain.user.repository;

import com.back.baton.domain.user.entity.User;
import com.back.baton.domain.user.entity.UserRole;
import com.back.baton.domain.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNicknameAndDeletedAt(String nickname, LocalDateTime deletedAt);
    Optional<User> findByEmail(String email);

    // 관리자 유저 목록 조회 필터 검색.
    @Query("""
            SELECT u
            FROM User u
            WHERE (:status IS NULL OR u.status = :status)
              AND (:role IS NULL OR u.role = :role)
              AND (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<User> searchAdminUsers(
            @Param("status") UserStatus status,
            @Param("role") UserRole role,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 관리자 대시보드 유저 상태별 수 집계.
    long countByStatus(UserStatus status);
}
