package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.response.TalentDetailRes;
import com.back.baton.domain.talent.dto.response.AuthorInfo;
import com.back.baton.domain.talent.entity.TalentStatus;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TalentController.class)
class TalentControllerDetailTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TalentService talentService;

    @MockitoBean // 또는 @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("상세 조회 성공 - 200과 data.author.* 포함")
    void getTalentDetail_200() throws Exception {
        TalentDetailRes res = new TalentDetailRes(
                1L, 9L, "개발", "웹페이지 개발", "내용...",
                3, 500, TalentStatus.ACTIVE, 10, 2, new BigDecimal("4.50"),
                LocalDateTime.now(), LocalDateTime.now(),
                new AuthorInfo(7L, "박재현", "https://img/7.png", "개발자입니다", new BigDecimal("36.50"))
        );
        given(talentService.getTalentDetail(1L)).willReturn(res);

        mockMvc.perform(get("/api/v1/talents/{talentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-2"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("개발"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.viewCount").value(10))
                .andExpect(jsonPath("$.data.author.authorId").value(7))
                .andExpect(jsonPath("$.data.author.nickname").value("박재현"));
    }

    @Test
    @DisplayName("없는/삭제된 재능이면 404를 반환한다")
    void getTalentDetail_404() throws Exception {
        willThrow(new CustomException(TalentErrorCode.TALENT_NOT_FOUND))
                .given(talentService).getTalentDetail(99L);

        mockMvc.perform(get("/api/v1/talents/{talentId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TALENT-404-001"));
    }
}