package com.back.baton.domain.user.repository;

import com.back.baton.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNicknameAndDeletedAt(String nickname, LocalDateTime deletedAt);
    Optional<User> findByEmail(String email);
}
