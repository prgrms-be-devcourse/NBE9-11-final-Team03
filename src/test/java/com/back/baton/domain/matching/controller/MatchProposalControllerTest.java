package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.service.MatchProposalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchProposalController.class)
class MatchProposalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MatchProposalService matchProposalService;

    @Test
    @DisplayName("매칭 제안 생성 API - 성공")
    void createMatchProposal_Success() throws Exception {
        Long requesterId = 1L;
        MatchProposalCreateReq req = new MatchProposalCreateReq(
                10L,
                2L,
                20L,
                "재능 교환 제안드립니다."
        );

        MatchProposalRes res = new MatchProposalRes(
                1L,
                req.providerTalentId(),
                req.requesterTalentId(),
                requesterId,
                req.providerId(),
                MatchProposalStatus.REQUESTED,
                req.requestMessage(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(matchProposalService.createMatchProposal(eq(requesterId), any(MatchProposalCreateReq.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/v1/match-proposals")
                        .param("requesterId", String.valueOf(requesterId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-3"))
                .andExpect(jsonPath("$.message").value("매칭 제안이 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.providerId").value(req.providerId()))
                .andExpect(jsonPath("$.data.requesterId").value(requesterId))
                .andExpect(jsonPath("$.data.requestMessage").value(req.requestMessage()));
    }

    @Test
    @DisplayName("매칭 제안 수락 API - 성공")
    void acceptMatchProposal_Success() throws Exception {
        Long proposalId = 1L;
        Long providerId = 2L;

        MatchProposalRes res = new MatchProposalRes(
                proposalId,
                20L,
                null,
                1L,
                providerId,
                MatchProposalStatus.ACCEPTED,
                "재능 구매 제안드립니다.",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(matchProposalService.acceptMatchProposal(eq(proposalId), eq(providerId)))
                .thenReturn(res);

        mockMvc.perform(patch("/api/v1/match-proposals/{proposalId}/accept", proposalId)
                        .param("providerId", String.valueOf(providerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-4"))
                .andExpect(jsonPath("$.message").value("매칭 제안이 수락되었습니다."))
                .andExpect(jsonPath("$.data.id").value(proposalId))
                .andExpect(jsonPath("$.data.providerId").value(providerId))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("매칭 제안 거절 API - 성공")
    void rejectMatchProposal_Success() throws Exception {
        Long proposalId = 1L;
        Long providerId = 2L;

        MatchProposalRes res = new MatchProposalRes(
                proposalId,
                20L,
                null,
                1L,
                providerId,
                MatchProposalStatus.REJECTED,
                "재능 구매 제안드립니다.",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(matchProposalService.rejectMatchProposal(eq(proposalId), eq(providerId)))
                .thenReturn(res);

        mockMvc.perform(patch("/api/v1/match-proposals/{proposalId}/reject", proposalId)
                        .param("providerId", String.valueOf(providerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-5"))
                .andExpect(jsonPath("$.message").value("매칭 제안이 거절되었습니다."))
                .andExpect(jsonPath("$.data.id").value(proposalId))
                .andExpect(jsonPath("$.data.providerId").value(providerId))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("매칭 제안 생성 API - 필수 값 누락 시 400 Bad Request 반환")
    void createMatchProposal_ValidationError() throws Exception {
        Long requesterId = 1L;
        MatchProposalCreateReq req = new MatchProposalCreateReq(
                10L,
                2L,
                20L,
                ""
        );

        mockMvc.perform(post("/api/v1/match-proposals")
                        .param("requesterId", String.valueOf(requesterId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}