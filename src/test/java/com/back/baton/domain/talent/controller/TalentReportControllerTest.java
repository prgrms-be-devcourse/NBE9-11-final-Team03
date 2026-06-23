package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.TalentReportReq;
import com.back.baton.domain.talent.dto.response.TalentReportRes;
import com.back.baton.domain.talent.entity.ReportReason;
import com.back.baton.domain.talent.entity.ReportStatus;
import com.back.baton.domain.talent.service.TalentReportService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TalentReportController.class)
class TalentReportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    @MockitoBean TalentReportService talentReportService;
    @MockitoBean JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("정상 요청이면 인증 사용자 기준으로 신고를 접수한다(201, PENDING)")
    @WithMockSecurityUser(userId = 1)
    void report_success() throws Exception {
        given(talentReportService.reportTalent(any(), any(), any()))
                .willReturn(new TalentReportRes(10L, 1L, ReportReason.INAPPROPRIATE_CONTENT,
                        ReportStatus.PENDING, LocalDateTime.now()));
        var request = new TalentReportReq(ReportReason.INAPPROPRIATE_CONTENT, "부적절합니다");

        mockMvc.perform(post("/api/v1/talents/1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("201-6"))
                .andExpect(jsonPath("$.data.reportId").value(10))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("신고 사유가 없으면 400을 반환한다")
    @WithMockSecurityUser(userId = 1)
    void report_nullReason_400() throws Exception {
        var request = new TalentReportReq(null, "사유");

        mockMvc.perform(post("/api/v1/talents/1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400-002"));
    }

    @Test
    @DisplayName("본인 재능 신고면 403을 반환한다")
    @WithMockSecurityUser(userId = 1)
    void report_self_403() throws Exception {
        given(talentReportService.reportTalent(any(), any(), any()))
                .willThrow(new CustomException(TalentErrorCode.SELF_REPORT_NOT_ALLOWED));
        var request = new TalentReportReq(ReportReason.ETC, null);

        mockMvc.perform(post("/api/v1/talents/1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TALENT-403-003"));
    }

    @Test
    @DisplayName("중복 신고면 409를 반환한다")
    @WithMockSecurityUser(userId = 1)
    void report_duplicate_409() throws Exception {
        given(talentReportService.reportTalent(any(), any(), any()))
                .willThrow(new CustomException(TalentErrorCode.DUPLICATE_REPORT));
        var request = new TalentReportReq(ReportReason.ETC, null);

        mockMvc.perform(post("/api/v1/talents/1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TALENT-409-002"));
    }
}