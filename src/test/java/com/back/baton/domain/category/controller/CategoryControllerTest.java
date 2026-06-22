package com.back.baton.domain.category.controller;

import com.back.baton.domain.category.dto.response.CategoryRes;
import com.back.baton.domain.category.service.CategoryService;
import com.back.baton.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CategoryService categoryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("카테고리 목록 조회 성공 - 200, 인증 헤더 불필요")
    void getCategories_success() throws Exception {
        given(categoryService.getActiveCategories()).willReturn(List.of(
                new CategoryRes(1L, "개발", 1),
                new CategoryRes(2L, "디자인", 2),
                new CategoryRes(3L, "문서정리", 3)
        ));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200-13"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("개발"))
                .andExpect(jsonPath("$.data[0].sortOrder").value(1))
                .andExpect(jsonPath("$.data[2].name").value("문서정리"));
    }

    @Test
    @DisplayName("활성 카테고리가 없으면 빈 배열을 반환한다")
    void getCategories_empty() throws Exception {
        given(categoryService.getActiveCategories()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-13"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}