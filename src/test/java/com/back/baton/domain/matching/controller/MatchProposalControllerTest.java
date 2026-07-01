package com.back.baton.domain.matching.controller;

import com.back.baton.domain.matching.dto.request.MatchProposalCreateReq;
import com.back.baton.domain.matching.dto.response.MatchProposalReceivedRes;
import com.back.baton.domain.matching.dto.response.MatchProposalRes;
import com.back.baton.domain.matching.dto.response.MatchProposalSentRes;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @DisplayName("현재 로그인한 사용자가 매칭 제안을 생성한다")
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
    @DisplayName("현재 로그인한 제공자가 받은 매칭 제안 목록을 조회한다")
    @WithMockSecurityUser(userId = 2)
    void getReceivedProposals_Success() throws Exception {
        Long providerId = 2L;

        List<MatchProposalReceivedRes> res = List.of(
                new MatchProposalReceivedRes(
                        1L,
                        MatchProposalStatus.REQUESTED,
                        "교환 제안드립니다.",
                        1L,
                        "요청자",
                        null,
                        10L,
                        "Spring Boot API 구현 도와드립니다",
                        providerId,
                        20L,
                        "React 화면 구현 도와드립니다",
                        LocalDateTime.now()
                )
        );

        when(matchProposalService.getReceivedProposals(eq(providerId), eq(MatchProposalStatus.REQUESTED)))
                .thenReturn(res);

        mockMvc.perform(get("/api/v1/match-proposals/received")
                        .param("status", "REQUESTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-14"))
                .andExpect(jsonPath("$.data[0].proposalId").value(1L))
                .andExpect(jsonPath("$.data[0].status").value("REQUESTED"))
                .andExpect(jsonPath("$.data[0].requestMessage").value("교환 제안드립니다."))
                .andExpect(jsonPath("$.data[0].requesterId").value(1L))
                .andExpect(jsonPath("$.data[0].requesterNickname").value("요청자"))
                .andExpect(jsonPath("$.data[0].requesterProfileImageUrl").isEmpty())
                .andExpect(jsonPath("$.data[0].requesterTalentId").value(10L))
                .andExpect(jsonPath("$.data[0].requesterTalentTitle").value("Spring Boot API 구현 도와드립니다"))
                .andExpect(jsonPath("$.data[0].providerId").value(providerId))
                .andExpect(jsonPath("$.data[0].providerTalentId").value(20L))
                .andExpect(jsonPath("$.data[0].providerTalentTitle").value("React 화면 구현 도와드립니다"));
    }

    @Test
    @DisplayName("현재 로그인한 요청자가 보낸 매칭 제안 목록을 조회한다")
    @WithMockSecurityUser(userId = 1)
    void getSentProposals_Success() throws Exception {
        Long requesterId = 1L;

        List<MatchProposalSentRes> res = List.of(
                new MatchProposalSentRes(
                        1L,
                        MatchProposalStatus.ACCEPTED,
                        "교환 제안드립니다.",
                        requesterId,
                        10L,
                        "Spring Boot API 구현 도와드립니다",
                        2L,
                        "제공자",
                        null,
                        20L,
                        "React 화면 구현 도와드립니다",
                        LocalDateTime.now()
                )
        );

        when(matchProposalService.getSentProposals(eq(requesterId), eq(MatchProposalStatus.ACCEPTED)))
                .thenReturn(res);

        mockMvc.perform(get("/api/v1/match-proposals/sent")
                        .param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-15"))
                .andExpect(jsonPath("$.data[0].proposalId").value(1L))
                .andExpect(jsonPath("$.data[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data[0].requestMessage").value("교환 제안드립니다."))
                .andExpect(jsonPath("$.data[0].requesterId").value(requesterId))
                .andExpect(jsonPath("$.data[0].requesterTalentId").value(10L))
                .andExpect(jsonPath("$.data[0].requesterTalentTitle").value("Spring Boot API 구현 도와드립니다"))
                .andExpect(jsonPath("$.data[0].providerId").value(2L))
                .andExpect(jsonPath("$.data[0].providerNickname").value("제공자"))
                .andExpect(jsonPath("$.data[0].providerProfileImageUrl").isEmpty())
                .andExpect(jsonPath("$.data[0].providerTalentId").value(20L))
                .andExpect(jsonPath("$.data[0].providerTalentTitle").value("React 화면 구현 도와드립니다"));
    }

    @Test
    @DisplayName("현재 로그인한 제공자가 매칭 제안을 수락한다")
    @WithMockSecurityUser(userId = 2)
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
                "purchase proposal",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(matchProposalService.acceptMatchProposal(eq(proposalId), eq(providerId)))
                .thenReturn(res);

        mockMvc.perform(patch("/api/v1/match-proposals/{proposalId}/accept", proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-4"))
                .andExpect(jsonPath("$.data.id").value(proposalId))
                .andExpect(jsonPath("$.data.providerId").value(providerId))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("현재 로그인한 제공자가 매칭 제안을 거절한다")
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
    @DisplayName("매칭 제안 생성 요청값이 유효하지 않으면 400을 반환한다")
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