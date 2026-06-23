package com.back.baton.domain.user.repository;

import com.back.baton.domain.user.entity.UserStatus;
import com.back.baton.domain.user.entity.WithdrawnUser;
import com.back.baton.global.config.JpaAuditingConfig;
import com.back.baton.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class WithdrawnUserRepositoryTest {

    @Autowired
    private WithdrawnUserRepository repository;

    @Autowired
    private TestEntityManager entityManager; // 영속성 상태 관리용

    @Test
    @DisplayName("생성일 기준 만료 데이터 삭제 테스트")
    void notDeleteValidUsersTest() {
        // 1. 데이터 준비 (일부러 만료된 데이터와 아닌 것을 섞음)
        LocalDateTime now = LocalDateTime.now();
        WithdrawnUser validUser = new WithdrawnUser("asgwerwer", UserStatus.ACTIVE);    // 삭제 제외
        WithdrawnUser bannedUser = new WithdrawnUser("asgwerweasfdsr", UserStatus.BANNED);    // 삭제 제외(영구정지)

        repository.save(validUser);
        repository.save(bannedUser);

        // 중요: DB에 강제로 반영 및 영속성 컨텍스트 비우기
        entityManager.flush();
        entityManager.clear();

        // 2. 실행
        repository.deleteByCreatedAtBeforeAndPermanentBanIsFalse(now);

        // 3. 검증
        assertThat(repository.findById(bannedUser.getId())).isPresent();
        assertThat(repository.findById(validUser.getId())).isPresent();
    }
    @Test
    @DisplayName("생성일 기준 만료 데이터 삭제 테스트")
    void deleteExpiredUsersTest() {
        // 1. 데이터 준비 (일부러 만료된 데이터와 아닌 것을 섞음)
        LocalDateTime now = LocalDateTime.now();
        WithdrawnUser expiredUser = new WithdrawnUser("awtewttwe", UserStatus.ACTIVE); // 삭제 대상
        WithdrawnUser bannedUser = new WithdrawnUser("asgwerweasfdsr", UserStatus.BANNED);    // 삭제 제외(영구정지)

        repository.save(expiredUser);
        repository.save(bannedUser);

        // 중요: DB에 강제로 반영 및 영속성 컨텍스트 비우기
        entityManager.flush();
        entityManager.clear();

        // 2. 실행
        repository.deleteByCreatedAtBeforeAndPermanentBanIsFalse(now.plusDays(100));

        // 3. 검증
        assertThat(repository.findById(expiredUser.getId())).isEmpty();
        assertThat(repository.findById(bannedUser.getId())).isPresent();
    }
}