package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.TalentUpdateReq;
import com.back.baton.domain.talent.dto.response.TalentUpdateRes;
import com.back.baton.domain.talent.service.TalentService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.back.baton.support.security.WithMockSecurityUser;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TalentController.class)
class TalentControllerUpdateTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    TalentService talentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("정상 요청이면 인증 사용자 기준으로 재능을 수정한다")
    @WithMockSecurityUser(userId = 1)
    void update_success() throws Exception {
        var request = new TalentUpdateReq(9L, "수정", "내용", 3, 200);
        var response = new TalentUpdateRes(10L, 9L, "수정", "내용", 3, 200, "ACTIVE");
        given(talentService.updateTalent(eq(10L), eq(1L), any())).willReturn(response);

        mockMvc.perform(put("/api/v1/talents/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-2"))
                .andExpect(jsonPath("$.data.title").value("수정"))
                .andExpect(jsonPath("$.data.creditPrice").value(200));
    }

    @Test
    @DisplayName("제목이 비어 있으면 400을 반환한다")
    @WithMockSecurityUser(userId = 1)
    void update_blankTitle_400() throws Exception {
        var request = new TalentUpdateReq(9L, "", "내용", 3, 200);

        mockMvc.perform(put("/api/v1/talents/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400-002"));
    }

    @Test
    @DisplayName("작성자가 아니면 403을 반환한다")
    @WithMockSecurityUser(userId = 2)
    void update_forbidden_403() throws Exception {
        var request = new TalentUpdateReq(9L, "수정", "내용", 3, 200);
        willThrow(new CustomException(TalentErrorCode.TALENT_FORBIDDEN))
                .given(talentService).updateTalent(eq(10L), eq(2L), any());

        mockMvc.perform(put("/api/v1/talents/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TALENT-403-001"));
    }
}
