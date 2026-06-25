package com.back.baton.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "프로필 수정 요청 DTO, 수정하지 않을 항목은 null로 보낸다.")
public record ProfileUpdateReq(

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String profileImageUrl,

    @Schema(
            description = "사용자 한줄 소개. 5자 이상",
            example = "백엔드 개발을 도와드립니다.",
            minLength = 5,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String introduction,

    @Schema(description = "내가 가진 재능 카테고리 ID 리스트", example = "[1,2]"
            ,requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<Long> myTalentCategoryIds,

    @Schema(description = "구매/교환하고 싶은 재능 카테고리 ID 리스트",
            example = "[3]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<Long> wantTalentCategoryIds,

    @Schema(description = "포트폴리오 링크 리스트",
            example = "[\"https://github.com/example\", \"https://notion.so/my-portfolio\"]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<String> portfolioLinkList

) {}