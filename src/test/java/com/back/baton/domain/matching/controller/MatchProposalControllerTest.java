package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.entity.MatchProposalStatus;
import com.back.baton.domain.matching.service.MatchProposalService;
import com.back.baton.global.security.JwtTokenProvider;
import com.back.baton.support.security.WithMockSecurityUser;
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

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("create match proposal with current user as requester")
    @WithMockSecurityUser(userId = 1)
    void createMatchProposal_Success() throws Exception {
        Long requesterId = 1L;
        MatchProposalCreateReq req = new MatchProposalCreateReq(
                10L,
                2L,
                20L,
                "proposal message"
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-3"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.providerId").value(req.providerId()))
                .andExpect(jsonPath("$.data.requesterId").value(requesterId))
                .andExpect(jsonPath("$.data.requestMessage").value(req.requestMessage()));
    }

    @Test
    @DisplayName("accept match proposal with current user as provider")
    @WithMockSecurityUser(userId = 2)
    void acceptMatchProposal_Success() throws Exception {
        Long proposalId = 1L;
        Long providerId = 2L;
        String idempotencyKey = "accept-proposal-1";

        MatchProposalRes res = new MatchProposalRes(
                proposalId,
                20L,
                null,
                1L,
                providerId,
                MatchProposalStatus.ACCEPTED,
                "purchase proposal",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(matchProposalService.acceptMatchProposal(eq(proposalId), eq(providerId), eq(idempotencyKey)))
                .thenReturn(res);

        mockMvc.perform(patch("/api/v1/match-proposals/{proposalId}/accept", proposalId)
                        .header("Idempotency-Key", idempotencyKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-4"))
                .andExpect(jsonPath("$.data.id").value(proposalId))
                .andExpect(jsonPath("$.data.providerId").value(providerId))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("reject match proposal with current user as provider")
    @WithMockSecurityUser(userId = 2)
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
                "purchase proposal",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(matchProposalService.rejectMatchProposal(eq(proposalId), eq(providerId)))
                .thenReturn(res);

        mockMvc.perform(patch("/api/v1/match-proposals/{proposalId}/reject", proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-5"))
                .andExpect(jsonPath("$.data.id").value(proposalId))
                .andExpect(jsonPath("$.data.providerId").value(providerId))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("create match proposal validation error")
    @WithMockSecurityUser(userId = 1)
    void createMatchProposal_ValidationError() throws Exception {
        MatchProposalCreateReq req = new MatchProposalCreateReq(
                10L,
                2L,
                20L,
                ""
        );

        mockMvc.perform(post("/api/v1/match-proposals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
