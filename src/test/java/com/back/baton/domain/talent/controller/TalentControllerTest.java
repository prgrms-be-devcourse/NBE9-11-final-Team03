package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.TalentCreateReq;
import com.back.baton.domain.talent.dto.response.TalentCreateRes;
import com.back.baton.domain.talent.service.TalentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TalentController.class)
class TalentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;
    @MockitoBean TalentService talentService;

    @Test
    @DisplayName("정상 요청이면 201과 함께 Location, 응답 본문을 반환한다")
    void create_success() throws Exception {
        given(talentService.createTalent(any(), any()))
                .willReturn(new TalentCreateRes(100L));
        var request = new TalentCreateReq(10L, "제목", "내용", 2, 100);

        mockMvc.perform(post("/api/v1/talents")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/talents/100"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("201-2"))
                .andExpect(jsonPath("$.data.talentId").value(100));
    }


    @Test
    @DisplayName("제목이 비어 있으면 400을 반환한다")
    void create_blankTitle_400() throws Exception {
        //given
        var request = new TalentCreateReq(10L, "", "내용", 2, 100);

        //when then
        mockMvc.perform(post("/api/v1/talents")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400-002"));
    }
}