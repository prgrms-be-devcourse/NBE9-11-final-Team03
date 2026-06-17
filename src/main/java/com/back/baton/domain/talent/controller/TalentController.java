package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.TalentCreateReq;
import com.back.baton.domain.talent.dto.request.TalentUpdateReq;
import com.back.baton.domain.talent.dto.response.TalentCreateRes;
import com.back.baton.domain.talent.dto.response.TalentUpdateRes;
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
}