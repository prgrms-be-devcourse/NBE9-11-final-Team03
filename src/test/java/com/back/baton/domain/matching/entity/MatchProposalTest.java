package com.back.baton.domain.matching.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchProposalTest {

    @Test
    @DisplayName("재능 교환 제안 생성 시 REQUESTED 상태이다.")
    void createMatchProposal() {
        MatchProposal matchProposal = MatchProposal.create(
                20L,
                10L,
                1L,
                2L,
                "재능 교환 제안드립니다."
        );

        assertThat(matchProposal.getProviderTalentId()).isEqualTo(20L);
        assertThat(matchProposal.getRequesterTalentId()).isEqualTo(10L);
        assertThat(matchProposal.getRequesterId()).isEqualTo(1L);
        assertThat(matchProposal.getProviderId()).isEqualTo(2L);
        assertThat(matchProposal.getRequestMessage()).isEqualTo("재능 교환 제안드립니다.");
        assertThat(matchProposal.getStatus()).isEqualTo(MatchProposalStatus.REQUESTED);
        assertThat(matchProposal.getRespondedAt()).isNull();
    }

    @Test
    @DisplayName("매칭 제안을 수락하면 ACCEPTED 상태가 되고 응답 시간이 기록된다.")
    void acceptMatchProposal() {
        MatchProposal matchProposal = MatchProposal.create(
                20L,
                10L,
                1L,
                2L,
                "재능 교환 제안드립니다."
        );

        matchProposal.accept();

        assertThat(matchProposal.getStatus()).isEqualTo(MatchProposalStatus.ACCEPTED);
        assertThat(matchProposal.getRespondedAt()).isNotNull();
    }

    @Test
    @DisplayName("매칭 제안을 거절하면 REJECTED 상태가 되고 응답 시간이 기록된다.")
    void rejectMatchProposal() {
        MatchProposal matchProposal = MatchProposal.create(
                20L,
                10L,
                1L,
                2L,
                "재능 교환 제안드립니다."
        );

        matchProposal.reject();

        assertThat(matchProposal.getStatus()).isEqualTo(MatchProposalStatus.REJECTED);
        assertThat(matchProposal.getRespondedAt()).isNotNull();
    }
}