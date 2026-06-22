package com.back.baton.domain.category.controller;

import com.back.baton.domain.category.dto.response.CategoryRes;
import com.back.baton.domain.category.service.CategoryService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "재능 카테고리 조회 API")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "카테고리 목록 조회",
            description = "활성 상태인 재능 카테고리를 노출 순서대로 조회합니다. 공개 API로 인증이 필요 없습니다."
    )
    public ResponseEntity<ApiResponse<List<CategoryRes>>> getCategories() {
        List<CategoryRes> response = categoryService.getActiveCategories();
        return ApiResponses.success(SuccessCode.CATEGORY_OK, response);
    }
}