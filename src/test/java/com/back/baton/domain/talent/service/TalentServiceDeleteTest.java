package com.back.baton.domain.talent.service;

import com.back.baton.domain.category.entity.Category;
import com.back.baton.domain.talent.entity.Talent;
import com.back.baton.domain.talent.repository.TalentRepository;
import com.back.baton.domain.trade.entity.TradeStatus;
import com.back.baton.domain.trade.repository.TradeRepository;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TalentServiceDeleteTest {

    @InjectMocks TalentService talentService;
    @Mock TalentRepository talentRepository;
    @Mock TradeRepository tradeRepository;

    @Test
    @DisplayName("본인 글이고 삭제 전이며 진행 중인 거래가 없으면 deletedAt이 기록된다")
    void deleteTalent_success() {
        // given
        Long authorId = 1L;
        Long talentId = 10L; // 명확하게 ID 지정
        Talent talent = Talent.create(authorId, mock(Category.class), "t", "c", 1, 0);
        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));

        given(tradeRepository.existsByTalentIdAndStatus(talentId, TradeStatus.IN_PROGRESS)).willReturn(false);

        // when
        talentService.deleteTalent(talentId, authorId);

        // then
        assertThat(talent.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("재능이 없으면 TALENT_NOT_FOUND")
    void deleteTalent_notFound() {
        // given
        given(talentRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertErrorCode(() -> talentService.deleteTalent(99L, 1L), TalentErrorCode.TALENT_NOT_FOUND);
    }

    @Test
    @DisplayName("진행 중인 거래(IN_PROGRESS)가 존재하면 TALENT_CANNOT_DELETE 예외가 발생한다")
    void deleteTalent_fail_tradeInProgress() {
        // given
        Long authorId = 1L;
        Long talentId = 10L;
        Talent talent = Talent.create(authorId, mock(Category.class), "t", "c", 1, 0);
        given(talentRepository.findById(talentId)).willReturn(Optional.of(talent));

        // 💡 진행 중인 거래가 있다고 가정 (true 반환)
        given(tradeRepository.existsByTalentIdAndStatus(talentId, TradeStatus.IN_PROGRESS)).willReturn(true);

        // when & then
        assertErrorCode(
                () -> talentService.deleteTalent(talentId, authorId),
                TalentErrorCode.TALENT_CANNOT_DELETE
        );
    }

    @Test
    @DisplayName("이미 삭제된 글이면 소유권 검사 전에 막힌다")
    void deleteTalent_alreadyDeleted() {
        // given: 작성자 1L 글을 미리 삭제 상태로
        Talent talent = Talent.create(1L, mock(Category.class), "t", "c", 1, 0);
        talent.softDelete();
        given(talentRepository.findById(10L)).willReturn(Optional.of(talent));

        // when & then: 남(2L)이 요청해도 삭제여부에서 먼저 차단 -> 404
        assertErrorCode(() -> talentService.deleteTalent(10L, 2L), TalentErrorCode.TALENT_NOT_FOUND);
    }

    @Test
    @DisplayName("본인 글이 아니면 TALENT_FORBIDDEN")
    void deleteTalent_forbidden() {
        // given: 작성자는 1L
        Talent talent = Talent.create(1L, mock(Category.class), "t", "c", 1, 0);
        given(talentRepository.findById(10L)).willReturn(Optional.of(talent));

        // when & then: 요청자 2L → 소유권에서 막힘
        assertErrorCode(() -> talentService.deleteTalent(10L, 2L), TalentErrorCode.TALENT_FORBIDDEN);
    }

    @Test
    @DisplayName("softDelete는 멱등 - 재호출해도 최초 삭제 시각을 덮지 않는다")
    void softDelete_idempotent() {
        // given
        Talent talent = Talent.create(1L, mock(Category.class), "t", "c", 1, 0);
        talent.softDelete();
        LocalDateTime first = talent.getDeletedAt();

        // when
        talent.softDelete();

        // then
        assertThat(talent.getDeletedAt()).isEqualTo(first);
    }

    private void assertErrorCode(ThrowingCallable callable, TalentErrorCode expected) {
        assertThatThrownBy(callable)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(expected));
    }
}