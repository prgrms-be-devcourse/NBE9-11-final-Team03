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
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TalentController.class)
class TalentControllerUpdateTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @MockitoBean TalentService talentService;
    @MockitoBean // 또는 @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Test
    @DisplayName("정상 수정이면 200과 변경된 데이터를 반환한다")
    void update_success() throws Exception {
        // given: 서비스가 수정 결과를 반환하도록
        var request = new TalentUpdateReq(9L, "수정", "내용", 3, 200);
        var response = new TalentUpdateRes(10L, 9L, "수정", "내용", 3, 200, "ACTIVE");
        given(talentService.updateTalent(eq(10L), eq(1L), any())).willReturn(response);

        // when & then: PUT 요청 -> 200, 응답 코드/본문이 의도대로 매핑되는지 확인
        mockMvc.perform(put("/api/v1/talents/10")
                        .header("X-User-Id", "1")
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
    void update_blankTitle_400() throws Exception {
        // given: title 공백 → @Valid 검증 실패. 컨트롤러 진입에서 막혀 서비스까지 가지 않음
        var request = new TalentUpdateReq(9L, "", "내용", 3, 200);

        // when & then: Bean Validation 실패 -> 400, 공통 검증 실패 코드
        mockMvc.perform(put("/api/v1/talents/10")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400-002"));
    }

    @Test
    @DisplayName("본인 글이 아니면 403을 반환한다")
    void update_forbidden_403() throws Exception {
        // given
        var request = new TalentUpdateReq(9L, "수정", "내용", 3, 200);
        willThrow(new CustomException(TalentErrorCode.TALENT_FORBIDDEN))
                .given(talentService).updateTalent(eq(10L), eq(2L), any());

        // when & then: 요청자 2L → 403, 도메인 에러코드 반환
        mockMvc.perform(put("/api/v1/talents/10")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TALENT-403-001"));
    }
}