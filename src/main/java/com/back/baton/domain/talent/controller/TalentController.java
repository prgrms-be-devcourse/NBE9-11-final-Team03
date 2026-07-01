package com.back.baton.domain.talent.controller;

import com.back.baton.domain.talent.dto.request.TalentCreateReq;
import com.back.baton.domain.talent.dto.request.TalentSearchReq;
import com.back.baton.domain.talent.dto.request.TalentUpdateReq;
import com.back.baton.domain.talent.dto.response.TalentCreateRes;
import com.back.baton.domain.talent.dto.response.TalentDetailRes;
import com.back.baton.domain.talent.dto.response.TalentListRes;
import com.back.baton.domain.talent.dto.response.TalentUpdateRes;
import com.back.baton.domain.talent.entity.TalentSortType;
import com.back.baton.domain.talent.service.TalentService;
import com.back.baton.global.response.ApiResponse;
import com.back.baton.global.response.ApiResponses;
import com.back.baton.global.response.CursorPageRes;
import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.security.CurrentUser;
import com.back.baton.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/talents")
@RequiredArgsConstructor
@Tag(name = "Talent", description = "재능 등록, 수정, 삭제, 목록/검색/상세 조회 API")
public class TalentController {

    private final TalentService talentService;

    @PostMapping
    @Operation(
            summary = "재능 등록",
            description = "현재 로그인한 사용자의 재능을 등록합니다."
    )
    public ResponseEntity<ApiResponse<TalentCreateRes>> createTalent(
            @CurrentUser SecurityUser currentUser,
            @Valid @RequestBody TalentCreateReq request
    ) {
        Long authorId = currentUser.getUserId();
        TalentCreateRes response = talentService.createTalent(authorId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/talents/" + response.talentId()))
                .body(ApiResponse.success(SuccessCode.TALENT_CREATED, response));
    }

    @PutMapping("/{talentId}")
    @Operation(
            summary = "재능 수정",
            description = "현재 로그인한 작성자가 등록한 재능의 카테고리, 제목, 내용, 예상 소요 시간, 크레딧 가격을 수정합니다."
    )
    public ResponseEntity<ApiResponse<TalentUpdateRes>> updateTalent(
            @Parameter(description = "수정할 재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @CurrentUser SecurityUser currentUser,
            @Valid @RequestBody TalentUpdateReq request
    ) {
        Long authorId = currentUser.getUserId();
        TalentUpdateRes response = talentService.updateTalent(talentId, authorId, request);
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }

    @DeleteMapping("/{talentId}")
    @Operation(
            summary = "재능 삭제",
            description = "현재 로그인한 작성자가 등록한 재능을 삭제합니다. 현재 구현은 soft delete 정책을 따릅니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteTalent(
            @Parameter(description = "삭제할 재능 ID", example = "1", required = true)
            @PathVariable Long talentId,
            @CurrentUser SecurityUser currentUser
    ) {
        Long authorId = currentUser.getUserId();
        talentService.deleteTalent(talentId, authorId);
        return ApiResponses.success(SuccessCode.TALENT_OK, null);
    }

    @GetMapping
    @Operation(
            summary = "재능 목록 조회",
            description = "커서 기반 페이지네이션으로 활성 재능 목록을 조회합니다. sort로 최신/평점/인기순 정렬을 지정합니다."
    )
    public ResponseEntity<ApiResponse<CursorPageRes<TalentListRes>>> getTalentList(
            @Parameter(description = "마지막으로 조회한 커서. 첫 요청은 생략합니다.", example = "30")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "조회할 재능 개수", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (LATEST/RATING/POPULAR)", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") TalentSortType sort
    ) {
        CursorPageRes<TalentListRes> response = talentService.getTalentList(cursor, size, sort);
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }

    @GetMapping("/search")
    @Operation(
            summary = "재능 검색 및 필터링",
            description = "카테고리, 크레딧 범위, 최소 평점, 완료 이력 여부로 검색하고 sort로 정렬합니다."
    )
    public ResponseEntity<ApiResponse<CursorPageRes<TalentListRes>>> searchTalents(
            @Valid @ParameterObject @ModelAttribute TalentSearchReq req,
            @Parameter(description = "마지막으로 조회한 커서. 첫 요청은 생략합니다.", example = "30")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "조회할 재능 개수", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (LATEST/RATING/POPULAR)", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") TalentSortType sort
    ) {
        CursorPageRes<TalentListRes> response = talentService.searchTalents(req, cursor, size, sort);
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }

    @GetMapping("/{talentId}")
    @Operation(
            summary = "재능 상세 조회",
            description = "재능 ID로 재능 상세 정보와 작성자 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<TalentDetailRes>> getTalentDetail(
            @PathVariable Long talentId,
            @Parameter(description = "조회수 증가 여부. 실제 상세 페이지 조회일 때만 true", example = "false")
            @RequestParam(defaultValue = "false") boolean increaseView
    ) {
        TalentDetailRes response = talentService.getTalentDetail(talentId, increaseView);
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }

    @GetMapping("/me")
    @Operation(summary = "내 재능 목록 조회",
            description = "현재 로그인한 사용자가 등록한(삭제되지 않은) 재능 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TalentListRes>>> getMyTalents(
            @CurrentUser SecurityUser currentUser
    ) {
        List<TalentListRes> response = talentService.getMyTalents(currentUser.getUserId());
        return ApiResponses.success(SuccessCode.TALENT_OK, response);
    }
}
