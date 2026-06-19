package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.TalentCreateReq;
import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.request.TalentUpdateReq;
import com.back.baton.domain.talent.dto.response.*;
import com.back.baton.domain.talent.service.TalentService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/talents")
@RequiredArgsConstructor
public class TalentController {

    private final TalentService talentService;

    @PostMapping
    public ResponseEntity<ApiResponse<TalentCreateRes>> createTalent(
            // TODO: JWT/Security PR 머지되면 @AuthenticationPrincipal Long userId 로 교체
            @RequestHeader("X-User-Id") Long authorId,
            @Valid @RequestBody TalentCreateReq request) {

        TalentCreateRes response = talentService.createTalent(authorId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/talents/" + response.talentId()))
                .body(ApiResponse.success(SuccessCode.TALENT_CREATED, response));
    }
    @PutMapping("/{talentId}")
    public ResponseEntity<ApiResponse<TalentUpdateRes>> updateTalent(
            @PathVariable Long talentId,
            @RequestHeader("X-User-Id") Long authorId,  // TODO: @AuthenticationPrincipal
            @Valid @RequestBody TalentUpdateReq request) {

        TalentUpdateRes response = talentService.updateTalent(talentId, authorId, request);
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }

    @DeleteMapping("/{talentId}")
    public ResponseEntity<ApiResponse<Void>> deleteTalent(
            @PathVariable Long talentId,
            @RequestHeader("X-User-Id") Long authorId){ // TODO: @AuthenticationPrincipal
                talentService.deleteTalent(talentId, authorId);
                return ApiResponses.success(SuccessCode.TALENT_OK, null);
    }

    // 재능 목록 조회 및 페이징
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageRes<TalentListRes>>> getTalentList(
            @RequestParam(required = false) Long cursor,        // 첫 요청은 생략 -> null
            @RequestParam(defaultValue = "20") int size) {

        CursorPageRes<TalentListRes> response = talentService.getTalentList(cursor, size);
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }

    // 재능 검색 및 필터링
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPageRes<TalentListRes>>> searchTalents(
            @Valid @ModelAttribute TalentSearchReq req,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {

        CursorPageRes<TalentListRes> response = talentService.searchTalents(req, cursor, size);
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }

    // 재능 상세 조회
    @GetMapping("/{talentId}")
    public ResponseEntity<ApiResponse<TalentDetailRes>> getTalentDetail(@PathVariable Long talentId) {
        TalentDetailRes response = talentService.getTalentDetail(talentId);
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }

}