package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.service.TalentService;
import com.back.baton.global.exception.CustomException;
import com.back.baton.global.response.code.TalentErrorCode;
import com.back.baton.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.back.baton.support.security.WithMockSecurityUser;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TalentController.class)
class TalentControllerDeleteTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TalentService talentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("정상 삭제 요청이면 인증 사용자 기준으로 삭제한다")
    @WithMockSecurityUser(userId = 1)
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/talents/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-2"))
                .andExpect(jsonPath("$.data").isEmpty());

        then(talentService).should().deleteTalent(10L, 1L);
    }

    @Test
    @DisplayName("작성자가 아니면 403을 반환한다")
    @WithMockSecurityUser(userId = 2)
    void delete_forbidden_403() throws Exception {
        willThrow(new CustomException(TalentErrorCode.TALENT_FORBIDDEN))
                .given(talentService).deleteTalent(10L, 2L);

        mockMvc.perform(delete("/api/v1/talents/10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TALENT-403-001"));
    }

    @Test
    @DisplayName("없는 재능이면 404를 반환한다")
    @WithMockSecurityUser(userId = 1)
    void delete_notFound_404() throws Exception {
        willThrow(new CustomException(TalentErrorCode.TALENT_NOT_FOUND))
                .given(talentService).deleteTalent(99L, 1L);

        mockMvc.perform(delete("/api/v1/talents/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TALENT-404-001"));
    }
}
