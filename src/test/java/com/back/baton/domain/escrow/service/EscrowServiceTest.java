package com.back.baton.domain.escrow.service;

import com.back.baton.domain.escrow.entity.Escrow;
import com.back.baton.domain.escrow.entity.EscrowStatus;
import com.back.baton.domain.escrow.repository.EscrowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EscrowServiceTest {

    @InjectMocks
    private EscrowService escrowService;

    @Mock
    private EscrowRepository escrowRepository;

    @Test
    @DisplayName("에스크로 생성 시 HELD 상태로 저장된다")
    void create_savedWithHeldStatus() {
        ArgumentCaptor<Escrow> captor = ArgumentCaptor.forClass(Escrow.class);
        given(escrowRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        escrowService.create(1L, 10L, 20L, 5000);

        then(escrowRepository).should().save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EscrowStatus.HELD);
    }

    @Test
    @DisplayName("에스크로 생성 시 구매 확정 만료일이 7일 후로 설정된다")
    void create_expiresAtSevenDaysLater() {
        ArgumentCaptor<Escrow> captor = ArgumentCaptor.forClass(Escrow.class);
        given(escrowRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        LocalDateTime before = LocalDateTime.now().plusDays(7).minusSeconds(1);
        escrowService.create(1L, 10L, 20L, 5000);
        LocalDateTime after = LocalDateTime.now().plusDays(7).plusSeconds(1);

        then(escrowRepository).should().save(captor.capture());
        LocalDateTime expiresAt = captor.getValue().getExpiresAt();
        assertThat(expiresAt).isAfter(before).isBefore(after);
    }

    @Test
    @DisplayName("에스크로 생성 시 전달된 필드가 올바르게 설정된다")
    void create_fields() {
        ArgumentCaptor<Escrow> captor = ArgumentCaptor.forClass(Escrow.class);
        given(escrowRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        escrowService.create(1L, 10L, 20L, 5000);

        then(escrowRepository).should().save(captor.capture());
        Escrow saved = captor.getValue();
        assertThat(saved.getTradeId()).isEqualTo(1L);
        assertThat(saved.getPayerId()).isEqualTo(10L);
        assertThat(saved.getPayeeId()).isEqualTo(20L);
        assertThat(saved.getAmount()).isEqualTo(5000);
    }
}